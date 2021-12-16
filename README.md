SuperFreezZ
===========

Android app (beta) making it possible to entirely freeze all background activities of any app.

 * Get back control over what runs on your phone
 * Enhance battery life and reduce mobile data usage by freezing rarely used apps
 * Especially useful while you are on a tour, where you need only some apps but a long battery life

Greenify can also do this, but it is not Open Source.

[<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="70">](https://f-droid.org/packages/superfreeze.tool.android/) 

How to install:
![Installation video](https://gitlab.com/SuperFreezZ/SuperFreezZ/uploads/f5b475ef170d7917c96f5932b26f5184/How_to_install.mp4)

SuperFreezZ is not yet another task manager promising to delete 10 GB of data per month or making your device 2x as fast. This is impossible.

Instead, SuperFreezZ is honest about its disadvantages: Freezing daily used apps probably drains your battery a little faster. Also, these apps will take longer to start when you use them the next time: SuperFreezZ will super freeze your apps, it takes about 1-3 seconds to un-freeze them. Greenify has the same disadvantages, except that the author of Greenify does not warn you about it. So: Just do not overdo it, and SuperFreezZ will be super useful.

Examples for apps that deserve to be frozen:

 * Untrusted apps (that you do not want to run in the background)
 * Apps you rarely use
 * Annoying apps

Features
--------

 * Optionally works without accessibility service as this slows down the device
 * Can freeze only apps not used for a week (can be configured)
 * Choose a white list (freeze all by standard) or a black list (do not freeze anything by standard)
 * Can freeze apps when the screen goes off
 * Options to freeze system apps and even SuperFreezZ itself
 * Completely open source and free software

Build
-----

The build should succeed out of the box with Android Studio and Gradle. If not, open an issue. Others will probably also have the problem.

Contributing to SuperFreezZ
------------

### Development

If you have a problem, question or an idea, just open an issue!

If you would like to help with developing, have a look at the issues, or think of something that could be improved, and open an issue for it.

Please tell me what you are going to do, to avoid implementing the same thing at the same time :-)

### Translate

You can [translate SuperFreezZ on Weblate](https://hosted.weblate.org/engage/superfreezz/). Current progress:

[![Translation status](https://hosted.weblate.org/widgets/superfreezz/-/multi-auto.svg)](https://hosted.weblate.org/engage/superfreezz/?utm_source=widget)

You can always add other lanuages.

### Donate

Developing SuperFreezZ is and was a lot of effort, which I did in my free time. Please donate to show me your support and to boost development.

At some point I promised here to upload it to the Play Store as soon as I earned 25€ through donations, but now I noticed that they probably won't let me upload it anymore because I am using some outdated APIs. But if you donated believing that I'll upload SF to the Play Store, tell me (just create an issue) and I'll try and replace them. Sorry for that!

[<img alt="Donate using Liberapay" src="https://liberapay.com/assets/widgets/donate.svg">](https://liberapay.com/Hocuri/)

Credits
-------

The code to show the app list is from [ApkExtractor](https://f-droid.org/wiki/page/axp.tool.apkextractor).

Robin Naumann made a nice intro. The intro was created using the AppIntro library.

The feature graphic background is from here: https://pixabay.com/photos/thunder-lighting-lightning-cloud-1368797/, the text was added with https://www.norio.be/android-feature-graphic-generator/.

Copying
-------

SuperFreezZ is licensed under GPLv3-or-later, see the project files and the LICENSE file for details.

Note that there are some files distributed under the MIT license because they were partly copied from ApkExtractor, which is ok because MIT is compatible to GPL:
```
src/superfreeze/tool/android/userInterface/mainActivity/AppsListAdapter.kt
src/superfreeze/tool/android/userInterface/mainActivity/MainActivity.kt
res/values/styles.xml
res/menu/main.xml
res/layout/list_item.xml
res/layout/activity_main.xml
```

Q/A
---

Q/A:

Q: What is the difference between hibernating and freezing?
A: There is none at all. If you hibernate an app with Greenify it will even be shown as frozen in SuperFreezZ and the other way round.

Q: But the correct spelling would be "SuperFreeze"!
A: I know.

Q: Do you have any intentions to sell freezers?
A: No.
