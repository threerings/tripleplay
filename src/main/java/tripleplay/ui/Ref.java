//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

/**
 * Used to obtain a reference to an element without messing up the nice declarative formatting one
 * can obtain when constructing an interface. For example:
 * <pre>{@code
 * Ref<Button> lgame = Ref.create(), ogame = Ref.create(), egame = Ref.create();
 * root.add(
 *     new Label(Style.FONT.is(megaFont)).setText("Atlantis"),
 *     new Group(AxisLayout.horizontal().alignTop().gap(50)).add(
 *         new Group(AxisLayout.vertical().offStretch()).add(
 *             new Label(lstyles).setText("Start new game:"),
 *             new Button().ref(lgame).setText("Local game"),
 *             new Button().ref(ogame).setText("Online game"),
 *             new Button().ref(egame).setText("Email game")),
 *         new Group(AxisLayout.vertical().offStretch()).add(
 *             new Label(lstyles).setText("Games in-progress:"))));
 * lgame.elem.click.connect(...);
 * }</pre>
 *
 * Formatting purists may consider this an improvement upon the following:
 * <pre>{@code
 * Button lgame, ogame, egame;
 * root.add(
 *     new Label(Style.FONT.is(megaFont)).setText("Atlantis"),
 *     new Group(AxisLayout.horizontal().alignTop().gap(50)).add(
 *         new Group(AxisLayout.vertical().offStretch()).add(
 *             new Label(lstyles).setText("Start new game:"),
 *             lgame = new Button().setText("Local game"),
 *             ogame = new Button().setText("Online game"),
 *             egame = new Button().setText("Email game")),
 *         new Group(AxisLayout.vertical().offStretch()).add(
 *             new Label(lstyles).setText("Games in-progress:"))));
 * lgame.click.connect(...);
 * }</pre>
 */
public class Ref<T extends Element>
{
    /** Creates a ref with the inferred type parameter. */
    public static <T extends Element> Ref<T> create () {
        return new Ref<T>();
    }

    /** The captured reference to the desired element. */
    public T elem;
}
