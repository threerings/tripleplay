//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.tools

import java.io.{BufferedWriter, FileWriter, File, IOException, PrintWriter}

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

import pythagoras.i.{Dimension, Rectangle}

import playn.core.json.JsonImpl

/**
* Generates a packed version of a series of image frames along with a JSON file which contains
* information on how to unpack the frames. The results can be used via {@link PackedFrames}.
*
* @param frame the size of a single frame in {@code _source}.
*/
class FramePacker (_source :File, _frame :Dimension) {
  import FramePacker._

  /** The maximum size of the texture atlas image. */
  var maxAtlasSize = 1024

  protected val _image :BufferedImage = ImageIO.read(_source)
  protected val _cols = _image.getWidth / _frame.width
  protected val _rows = _image.getHeight / _frame.height

  // sanity checking and reporting
  {
    if (_image.getWidth % _frame.width != 0) {
      System.err.println("Warning image not even number of frames wide [source=" + _source +
                         ", frameWidth=" + _frame.width +
                         ", imageWidth=" + _image.getWidth() + "]")
    }
    if (_image.getHeight % _frame.height != 0) {
      System.err.println("Warning image not even number of frames tall [source=" + _source +
                         ", frameHeight=" + _frame.height +
                         ", imageHeight=" + _image.getHeight() + "]")
    }
  }

  /** Creates a packer for the supplied source image. The width and height of the frames inside
   * {@code source} will be decoded from the filename, which must be of the form {@code
   * foo_WIDTHxHEIGHT.ext}.
   */
  def this(source :File) = this(source, FramePacker.decodeFrameSize(source.getName))

  /** Creates a packer for the supplied source image.
   *
   * @param frameWidth the width of a single frame in {@code source}.
   * @param frameHeight the height of a single frame in {@code source}.
   */
  def this (source :File, frameWidth :Int, frameHeight :Int) =
    this(source, new Dimension(frameWidth, frameHeight))

  /** Packs the source and generates the results in {@code target}. A metadata file will be
   * generated along side {@code target} with the file extension changed to {@code .json}.
   */
  def pack (target :File) {
    val bounds = 0 until (_rows*_cols) map(computeTrimmedBounds) filter(_ != null) toSeq
    val frames = bounds.zipWithIndex map(ib => Frame(ib._2, ib._1))
    val sframes = frames.sortWith(_.area > _.area)

    // estimate the optimal atlas size
    val area = frames.map(_.area).sum
    val side = math.sqrt(area).toInt
    var max = new Dimension(frames.map(_.pwidth).max, frames.map(_.pheight).max)

    // search a few increasingly large rectangles for one that fits our textures
    def findPacking (width :Int, height :Int) :Node = {
      if (width > maxAtlasSize || width > maxAtlasSize) {
        throw new RuntimeException("Oops. Need a texture of size " + width + "x" + height +
                                   ", but that exceeds max atlas size (" + maxAtlasSize + ")")
      }
      // use a k-d tree packing algorithm to pack the frames into a single image
      val root :Option[Node] = Some(new Empty(0, 0, width, height))
      (root /: sframes)((n, f) => n.flatMap(_.add(f))) match {
        case Some(n) => n
        case None => if (width < height) findPacking(width+width/5, height)
                     else findPacking(width, height+height/5)
      }
    }
    val node = findPacking(math.max(side, max.width), math.max(side, max.height))

    // generate the atlas image
    val nsize = node.size
    val atlas = new BufferedImage(nsize.width, nsize.height, _image.getType)
    val (srast, drast) = (_image.getRaster, atlas.getRaster)
    node.apply { n =>
      val (f, b) = (n.frame, n.frame.bounds)
      drast.setRect(n.x, n.y, srast.createChild(f.sx, f.sy, b.width, b.height, 0, 0, null))
    }
    ImageIO.write(atlas, "PNG", target)

    // generate the associated json file
    val json = new JsonImpl().newWriter
    json.`object`
    json.value("width", _frame.width).value("height", _frame.height)
    json.array("frames")
    node.apply { n =>
      val (f, b) = (n.frame, n.frame.bounds)
      json.`object`
      json.value("idx", f.index)
      json.array("off").value(b.x).value(b.y).end
      json.array("src").value(n.x).value(n.y).value(b.width).value(b.height).end
      json.end
    }
    json.end.end
    val jname = target.getName.reverse.dropWhile(_ != '.').reverse + "json"
    val jfile = new File(target.getParentFile, jname)
    val jout = new PrintWriter(new BufferedWriter(new FileWriter(jfile)))
    jout.println(json.write)
    jout.close
  }

  def computeTrimmedBounds (idx :Int) :Rectangle = {
    val (row, col) = (idx/_cols, idx%_cols)
    val (ox, oy) = (col * _frame.width, row * _frame.height)
    var (firstrow, lastrow, minx, maxx) = (-1, -1, _frame.width, 0)

    for (yy <- 0 until _frame.height) {
      var (firstidx, lastidx) = (-1, -1)
      for (xx <- 0 until _frame.width) {
        val argb = _image.getRGB(ox + xx, oy + yy)
        if ((argb >> 24) != 0) {
          // if we've not yet seen a non-transparent pixel, make a note that this is the first
          // non-transparent pixel in the row
          if (firstidx == -1) firstidx = xx
          // keep track of the last non-transparent pixel we saw
          lastidx = xx
        }
      }

      // if we have not yet seen a pixel on this row...
      if (firstidx != -1) {
        // update our min and maxx
        minx = math.min(firstidx, minx)
        maxx = math.max(lastidx, maxx)
        // keep track of the first and last row on which we see pixels
        if (firstrow == -1) firstrow = yy
        lastrow = yy
      }
    }

    // if we found no non-transparent pixels, return null to indicate so
    if (firstrow == -1) null
    else new Rectangle(minx, firstrow, maxx - minx + 1, lastrow - firstrow + 1)
  }

  case class Frame (index :Int, bounds :Rectangle) {
    // add a one pixel border between images to prevent bleeding
    val (pwidth, pheight) = (bounds.width+1, bounds.height+1)
    val area = pwidth * pheight
    val sx = (index % _cols) * _frame.width + bounds.x
    val sy = (index / _cols) * _frame.height + bounds.y
  }

  abstract class Node {
    def add (frame :Frame) :Option[Node]
    def size :Dimension
    def apply (func :(Leaf => Unit)) :Unit
    def toString (depth :Int) :String = (" " * depth)
    override def toString = toString(0)
  }

  class Empty (val x :Int, val y :Int, width :Int, height :Int) extends Node {
    override def add (frame :Frame) = (width - frame.pwidth, height - frame.pheight) match {
      case (0, 0) =>
        Some(new Leaf(x, y, frame))
      case (dw, dh) if (dw < 0 || dh < 0) =>
        None
      case (dw, dh) if (dw > dh) =>
        Some(new Branch(new Empty(x, y, frame.pwidth, height).add(frame).get,
                        new Empty(x + frame.pwidth, y, dw, height)))
      case (dw, dh) =>
        Some(new Branch(new Empty(x, y, width, frame.pheight).add(frame).get,
                        new Empty(x, y + frame.pheight, width, dh)))
    }
    override def size = new Dimension(0, 0)
    override def apply (func :(Leaf => Unit)) = ()
    override def toString (depth :Int) =
      super.toString(depth) + "Empty(" + width + "x" + height + "+" + x + "+" + y + ")"
  }

  class Branch (left :Node, right :Node) extends Node {
    override def add (frame :Frame) = (left.add(frame), right.add(frame)) match {
      case (Some(nl), _) => Some(new Branch(nl, right))
      case (_, Some(nr)) => Some(new Branch(left, nr))
      case (_, _) => None
    }
    override def size = {
      val (ls, rs) = (left.size, right.size)
      new Dimension(math.max(ls.width, rs.width), math.max(ls.height, rs.height))
    }
    override def apply (func :(Leaf => Unit)) = {
      left.apply(func)
      right.apply(func)
    }
    override def toString (depth :Int) =
      super.toString(depth) + "Branch\n" + left.toString(depth+1) + "\n" + right.toString(depth+1)
  }

  class Leaf (val x :Int, val y :Int, val frame :Frame) extends Node {
    override def add (frame :Frame) = None
    override def size = new Dimension(x + frame.bounds.width, y + frame.bounds.height)
    override def apply (func :(Leaf => Unit)) = func.apply(this)
    override def toString (depth :Int) =
      super.toString(depth) + " Leaf(" + x + ", " + y + ", " + frame + ")"
  }
}

object FramePacker {
  def main (args :Array[String]) {
    if (args.length != 2) {
      System.err.println("Usage: FramePacker source target")
      System.exit(255)
    }
    new FramePacker(new File(args(0))).pack(new File(args(1)))
  }

  def decodeFrameSize (name :String) :Dimension = {
    val didx = name.lastIndexOf(".")
    if (didx == -1) throw new IllegalArgumentException(ERR_INVALID_FILENAME + name)
    val uidx = name.lastIndexOf("_", didx)
    if (uidx == -1) throw new IllegalArgumentException(ERR_INVALID_FILENAME + name)
    val bits = name.substring(uidx+1, didx).split("x")
    if (bits.length != 2) throw new IllegalArgumentException(ERR_INVALID_FILENAME + name)
    try {
      new Dimension(Integer.parseInt(bits(0)), Integer.parseInt(bits(1)))
    } catch {
      case nfe :NumberFormatException =>
        throw new IllegalArgumentException(ERR_INVALID_FILENAME + name)
    }
  }

  // def nextPowerOfTwo (value :Int) = {
  //   var pow2 = 2
  //   while (pow2 < value) pow2 *= 2
  //   pow2
  // }

  final val ERR_INVALID_FILENAME = "File name must be of the form 'foo_WIDTHxHEIGHT.ext': "
}
