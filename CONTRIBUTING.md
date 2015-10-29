# Creating a Bug Report using Github Issues

When creating a bug report the more information the better! 

Copy and paste the `bug report template` into the [new issue](https://github.com/mapzen/eraser-map/issues/new). There's an `example bug report` below, and `tips` on where to find some of this information.

## Bug report template

Give your issue a descriptive name. 

Then in the "Leave a comment" section, copy-paste the following text and fill out the details.

* **Device name:** 
* **Android Version:** 
* **App build number:**
* **What did you expected to happen?** 
* **What happened instead?**
* **Steps to reproduce:**
* **Attach a screenshot**
* **Attach device logs**

If something happened while you were **routing**, share with us:

* **Where were you?**
* **Routing from origin:**
* **Routing to destination:**

If something happened while you were **searching**, share with us:

* **Where were you?**


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


### Device logs

This requires installing **ADB** or a **logcat** viewer app. If you don’t know what these things are don’t worry about it. If you do please attach logs!

### Why we ask for your location for routing and search

You don't have to provide it, but we try to prioritize search results nearer your `current location` and it's hard to replicate the bug unless we have this information. 

If you don't feel comfortable sharing your location in the issue, please email it to: [android-support@mapzen.com](mailto:android-support@mapzen.com).