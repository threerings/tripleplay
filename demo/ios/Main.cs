using System;
using MonoTouch.Foundation;
using MonoTouch.UIKit;

using playn.ios;
using playn.core;
using tripleplay.platform;

namespace tripleplay.demo
{
  [Register ("AppDelegate")]
  public partial class AppDelegate : IOSApplicationDelegate {
    public override bool FinishedLaunching (UIApplication app, NSDictionary options) {
      app.SetStatusBarHidden(true, true);
      IOSPlatform.Config config = new IOSPlatform.Config();
      config.orients = IOSPlatform.SupportedOrients.LANDSCAPES;
      IOSPlatform platform = IOSPlatform.register(app, config);
      IOSTPPlatform.register(platform);
      PlayN.run(new TripleDemo());
      return true;
    }
  }

  public class Application {
    static void Main (string[] args) {
      UIApplication.Main (args, null, "AppDelegate");
    }
  }
}
