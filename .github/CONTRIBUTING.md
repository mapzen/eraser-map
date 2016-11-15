# Creating a Bug Report using Github Issues

When creating a bug report the more information the better! 

Copy and paste the `bug report template` into the [new issue](https://github.com/mapzen/eraser-map/issues/new). There's an `example bug report` below, and `tips` on where to find some of this information.

## Bug report template

Give your issue a descriptive name. 

Then in the "Leave a comment" section, copy-paste the following text and fill out the details.

```
* **Device name:** 
* **Android Version:** 
* **App build number:**
* **What did you expected to happen?** 
* **What happened instead?**
* **Steps to reproduce:**
* **Attach a screenshot**
* **Attach device logs**
```

If something happened while you were **routing**, share with us:

```
* **Where were you?**
* **Routing from origin:**
* **Routing to destination:**
```

If something happened while you were **searching**, share with us:

```
* **Where were you?**
```

#### Example bug report

* **Device name:** `Samsung Galaxy S6 Edge`
* **Android version:** `5.1.1`
* **App build number:** `0.1.1`
* **What did you expected to happen?** `I tried to route to my home town.`
* **What happened instead?** `The route preview came up, but I couldn't see the route line.`
* **Steps to reproduce:** `I tapped on the magnifying glass icon, typed "Eureka", choose the first search result and tapped the route button in the bottom bar. The map zoomed out to show me Eureka and my current location (in San Francisco), but I expected to see a purple line connecting Eureka and San Francisco. But I did not, and that was confusing.`
* **Attach a screenshot:** `image`
* **Attach device logs:** `file`
* **Where you were?** `I was at my office: 201 Spear St, San Francisco CA.`
* **Your origin:** `201 Spear St, San Francisco CA`
* **Your destination:** `Eureka, CA`

##Tips


### Finding your Android Version number

Go to `Settings` > `About phone` > `Android version`.

### Finding your Eraser Map app build number

Coming soon to Eraser Map `settings` menu!

### To take a screenshot

* **Mainline Android (Nexus):** Hold the `power + volume-down` buttons for a few seconds
* **Samsung Android:** Hold the `power + home` buttons for a few seconds

The screen will flash. Go to your photo `Gallery.app` and email the screenshot to yourself. Then attach it to the issue.

### Why we ask for your location for routing and search

You don't have to provide it, but we try to prioritize search results nearer your `current location` and it's hard to replicate the bug unless we have this information. 

If you don't feel comfortable sharing your location in the issue, please email it to: [android-support@mapzen.com](mailto:android-support@mapzen.com).

### Device logs

This requires installing **ADB** or a **logcat** viewer app. If you don’t know what these things are don’t worry about it. If you do please attach logs!

For the adventurous: 

#### Install Android Studio or the SDK tools

You'll need Android Studio with the Android SDK Tools package, or possibly just the SDK. 

1. http://developer.android.com/sdk/index.html#Other
2. For the Android Studio route, install it from http://developer.android.com/sdk/installing/index.html?pkg=studio
3. In the Android Studio SDK Manager, install Tools > Android SDK Tools. http://developer.android.com/sdk/installing/adding-packages.html (or possibly only the SDK).

#### Enable debugging

1. Connect your phone to your machine with a USB cable.
2. Enable USB debugging on your phone in Settings > About phone and tap Build number seven times. Return to the previous screen to find Developer options at the bottom. 
3. Go to Settings > Developer Options (which should now be visible) and make sure the developer options are on and USB debugging is enabled.
4. At some point, you may see a confirmation to Allow USB debugging with a key of the computer. Tap OK.

#### Enable logging

Follow these steps to enable command-line debugging. You can also use Android Studio for your debugging, if you have installed it.

Here is the usage for the Android Debug Bridge: http://developer.android.com/tools/help/adb.html

1. Start a terminal and navigate to the directory with the Android SDK platform-tools. For the default Android Studio installation, type cd /Users/[name]/Library/Android/sdk/platform-tools.
2. Type adb devices. If correctly configured, you should see a list of the devices attached.
3. If you are having trouble, try one of the solutions in this StackExchange thread. http://stackoverflow.com/questions/7609270/not-able-to-access-adb-in-os-x-through-terminal-command-not-found I had this same scenario (note that I had already had Android Studio, and was able to solve it with the last answer on the page, with reinstalling with brew). 
4. Get ready to start Eraser Map and in the terminal, type adb logcat. This will dump all the logs to your terminal. (Note that there are probably ways to filter this. You might also want to type a word, like your name, that you can search for to be able to identify when the relevant logging starts.)
5. Start Eraser Map on your phone.
6. Copy the logs on your terminal to an email or bug report, as appropriate.
