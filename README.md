# FloatingGroupExpandableListView
FloatingGroupExpandableListView is ~~a huge name~~ an open source Android library that provides a floating group view (*aka* anchored / pinned / sticky section header) at the top of the `ExpandableListView`. This lib is based on Emil Sjölander's [StickyListHeaders](https://github.com/emilsjolander/StickyListHeaders).

![Sample Screenshot](https://github.com/diegocarloslima/FloatingGroupExpandableListView/raw/master/sample.png)&nbsp;![Sample Animation](https://github.com/diegocarloslima/FloatingGroupExpandableListView/raw/master/sample_animation.gif)

## Features
- Works with list headers, footers, dividers and padding.
- Works with partially transparent or hidden group views.
- Handles touch events on the floating group view properly.
- Allows group transition animation.

## Sample Application
[![Get it on Google Play](http://www.android.com/images/brand/get_it_on_play_logo_small.png)](https://play.google.com/store/apps/details?id=com.diegocarloslima.fgelv.sample)

The sample app project code is also included on this repository.

## Usage
The FloatingGroupExpandableListView is very easy to setup. You just need a few steps:

1. Add the `FloatingGroupExpandableListView` to your xml file. It will look something like this:

    ```xml
    <com.diegocarloslima.fgelv.lib.FloatingGroupExpandableListView
    android:id="@+id/my_list"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
    ```

2. Then add these lines to your java code (usually on your `Activity.onCreate()` or `Fragment.onCreateView()` method):

    ```java
    FloatingGroupExpandableListView myList = (FloatingGroupExpandableListView) findViewById(R.id.my_list);
    BaseExpandableListAdapter myAdapter = new MyAdapter();
    WrapperExpandableListAdapter wrapperAdapter = new WrapperExpandableListAdapter(myAdapter);
    myList.setAdapter(wrapperAdapter);
    ```

3. That's it! You're ready to go! For a complete implementation, you can take a look at the sample project.

## Gradle
Add the following dependency to your `build.gradle` file:

```groovy
dependencies {
    compile 'com.diegocarloslima:fgelv:0.1.+@aar'
}
```

## Used by

[Call recorder (2 in 1) (Free)](https://play.google.com/store/apps/details?id=com.CallVoiceRecorderFree)

[Tourism Coast Atlantic Forest](https://play.google.com/store/apps/details?id=br.com.jalan.oasis2.srcvb2014)

Let me know if you are using this lib in your app. I'll be glad to put your app name here :).

## Contributing

Pull requests with bug fixes or new features are always welcome :), but please, send me a separate pull request for each bug fix or feature. Also, you can [contact](mailto:diego@diegocarloslima.com) me to discuss a new feature before implementing it.

## Developed By

Diego Carlos Lima: <diego@diegocarloslima.com>

## License

    Copyright 2013 Diego Carlos Lima

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
