# ReLaunchX - filemanager / launcher for ONYX Boox Max 

ReLaunchX is a versatile reading programs launcher and system-wide shell for Onyx Boox Max. It is a continuation of ReLaunch 1.3.8 made for Nook Simple Touch (also supported by Sony PRS-T1).
It is assumed that the user library is organized in an ordered structure of folders and files have names of books that match their names.
ReLaunchX supports flexible configuration of many on-screen buttons for quick access to certain features of the program and run third party applications.

Non-obvious possibilities:
* Some functions of the program available only with run as a desktop shell for system.&lt;br&gt;
* Some functions require a device with root-access, or only implemented for Nook Simple Touch (you will receive appropriate notice).
* On-screen scrolling buttons support double click to navigate to the specified number of percents in list and long-click to quickly move to the end (beginning) of the file list. This feature often will not work properly, be careful (you need to enable it in settings).
* If you open with the button, for example, the second-to-read book, it will be after the opening of the first. Thus, you will start in round fashion two (three, etc., depending on the configuration), the last-readed books.

Note:
I aim to keep compatibility with previously supported devices however the only device on which I can run/debug the app is Onyx Boox Max as of today hence I decided to rename/rebrand the product in order to not cause any confusion.

# The tool is able to work in two modes:

### Application mode
When ReLaunch is invoked from other launcher (technically speaking when it invoked using main intent - android.intent.action.MAIN) it works in application mode.

1. filemanager
You see a directory structure, you can walk on it. Tap on directory name to go into the directory, tap on ".." labeled line to go one directory up.

2. on-file actions, single tap
When you tap on regular file (not a directory) the ReLaunch may perform a few actions, depending on you settings (see 4.)
  * call explicit package or create intent for that file if such action is defined in Settings->Associations screen
  * for .apk file - install it
  * for any other file - open viewer (if file is small enough, see 4.). Note that editor can be opened from the viewer
  * do nothing if file is too big for viewer and association for that file not found

3. on-file actions, long tap
On long tap context menu is opened. The file can be added to favorites, marked as read (we are speaking about ebooks, isn't it?) or removed (with confirmations, see 4.).
Note that directories can be removed too, even recursively. Be careful!

4. settings (first from left button on top panel)
Some application-wide settings. Pay attention to "Filter settings" and "Association".
Filter settings defines which files are visible in file manager. If you define some rules there do not forget to switch on "Filter result" checkbox in general settings section.
Association defines action which performs on file tap (according to file's extension)

5. search (second from left button on top panel
You can search files (books) from any root by some patterns. Note "Show all books" button. By pressing it you see all files with known extension (see 4.).

6. last opened files list (third from left button on top panel)
Each file opened by ReLaunch remembered in last opened files (LIFO order).

7. favorites list (fourth from left button on top panel
You can manage favorites list by adding / removing books to it.

8. about (fifth from left button on top panel)
Application info

### Launcher mode
When ReLanuch is invoked by pressing "Home" (in a NookSimpleTouch case by SoftKey "home" button), or by pressing "Nook button - > Shop" or "Nook button - > Library" it works in launcher mode. In that mode it has all standard launcher functionality (task manager / application manager) in addition to application mode possibilities described above.

Launcher mode functionality:
* Task manager (tap on memory status, first left bottom button)
* Last used applications (second left bottom button)
* All applications (third left bottom button)
  You can launch application by taping on it, or uninstall it by
  long tap (context menu opened)
* Favorites applications (fourth left bottom button)
* Battery status (tap on battery status, fifth left bottom button)


# CREDITS
* [ReLaunch project](https://github.com/yiselieren/ReLaunch)
* [CoolReader Engine project](http://sourceforge.net/projects/crengine/) (Eink support code)
* [Android Asset Studio](https://romannurik.github.io/AndroidAssetStudio/index.html) (icons)
* [DefaultIcon](http://www.defaulticon.com/) (icons)
* [Interactivemania](http://www.interactivemania.com/) (icon pack)


# CHANGELOG
* 1.2.1
 * Added setting to adjust buttons size (height).
 * Fixed launcher mode: back button does not cause about to quit message.
 * Simplified run mode: removed the setting about start mode. When started from another launcher the app always works in file manager mode. When started as launcher (at boot time or by pressing home button) it works as launcher.
* 1.2.0
 * Added nice graphical storage info view.
 * Added preference "Advanced Settings > Appearance > Storage Info View", enabled adds the old filesystem details print to Storage info view.
 * Fixed app hungs due to signal interrupted exec() by using Android API.
 * Fixed text visibility on a few views (in few places text was white on white).
* 1.1.0
 * Added more file sorting options
 * Added new setting for File Manager 'Show file details'
 * Minor bugfixes
* 1.0.0
 * fixed partition size and free space view for Onyx Boox Max
 * added Onyx Boox Max to supported devices
 * changed name to ReLaunchX and icon to differentiate from Legacy ReLaunch app
 * changed Application ID to not conflict with Legacy ReLaunch app
 * imported legacy project to AndroidStudio
 * forked ReLaunch 1.3.8 project


## Legacy changelog from ReLaunch

* 1.3.8
 * added file properties dialog
 * rewritten book annotation dialog
 * returned function "add to Start folders"
* 1.3.7
 * added annotation window for fb2/epub files
 * Back button may be used for folder navigation
 * Start Folder, selected from context menu, replaces the old ones
 * single line output mode for filenames
 * fixed bug, then longpress event fired after doubletap
* 1.3.6
 * updated preferences screen for SONY PRS-T1
 * new option \"Add start folder\" in context menu
 * new options for double and long taps on "Favorite applications" button
 * disabled fullscreen mode in Preferences activity
 * fixed bug with symbol # in file names
 * tunable number of columns of All and Favorite applications
 * added new variable %f to book name template
* 1.3.5
 * new appearance of preference screen
 * added support for Sony PRS-T1 buttons
 * added new file associations
 * fixed bug with second line of book title disappearance
 * added options for clear Favorites and Last opened lists
* 1.3.4
 * showing the book title in two lines
 * database cleanup function implemented
 * added items separator to files list
 * fixed font size for SONY-PRS-T1 in preferences screen
 * fixed rename file from tags with a template with line break
 * fixed sort order in "Show titles" mode
* 1.3.3
 * interface is partially adapted for SONY-PRS-T1
 * save/restore settings function implemented
 * show book titles in Favorites and Last recent opened lists
 * a new mechanism of context menu implementation
 * fixed bug with battery status intent reciever
 * fixed bug of incorrect book series sorting
 * fixed some minor bugs
* 1.3.2
 * almost fully implemented file operations (create, rename, copy, move file and folders)
 * book title pattern changed
 * added context menu item [Rename from fb2/epub tags]
 * added saving current position in the directory between calls bookreaders
 * fixed sorting in book titles mode
* 1.3.1
 * Option "Show hidden files" added to preferences
 * Copy/move file functions added to context menu
 * Show book titles functionality implemented for .fb2 and .epub files
* 1.3.0
 * source code cleaning and formatting
 * fixed filtering on directories, now not under filtering
 * some fixes in search (strange behavior)
 * quick scroll in "results screens" fixes
 * preferences "save dialog" only after changing something
 * added ability configure icon size
 * added on-screen scroll buttons for preferences
 * added ability to disable (buggy) percentage scroll on Nook
 * added extended functionality to Last Used, Favorites and Settings buttons
 * added Setup WiFi button to advanced screen
 * Advanced, Memory, Battery buttons now configurable
 * Full new icons and some layout changes - in original NOOK look and feel.
 * Scroll by on-screen buttons changed to satisfy Sony users.
 * Lock functionality in Advanced screen.
 * some changes in Home button configuration.
 * simplify buttons on Preferences screen.
 * force screen orientation change (configurable).
 * Some fixes in power adapter show when attached.
 * Configurable File Manager (now implemented deletion only, sorry) menu items (user can disable).
 * Configurable hiding of known (via associations) file extensions (user can enable).
 * Configurable hiding of known (via home dirs) parts of directories names (user can enable).
 * Fixes in context menu creation for empty dir and leaking of mount sdcard broadcast receiver.
* 1.2.10
 * skipped due quick development
* 1.2.9
 * Home icon changed.
 * Spanish translation added.
* 1.2.8
 * Added USB or AC adapter connection information.
 * Redone support for Eink Nook display.
 * Configurable multicolumn output.
 * Close Fav, Lru and so on windows after start reader (configurable).
 * Font size for files and directory names (configurable).
 * User may have now many home folders.
 * User may disable to go up from home folders.
 * Smart configurable Home button with distinct reaction to tap, double tap and long tap introduced.
 * Fixes: "scroll buttons" sometimes not working; invocation dir from favorites and search results, now shown in main window; search results representation when search from root.
* 1.2.7
 * Preferences Light Theme for Sony reader and mobiles.
 * Application and readers invocation code fixes.
 * Small layout and visual fixes.
* 1.2.6
 * Source code and resources reformatted, caused by i18n.
 * Russian translation.
 * WiFi signal strength receive fixed, now with dBm level and "classic" visual output.
 * Application and readers detection and invocation code rewritten, many bugfixes.
 * Fully redesigned to original White NOOK ST "look and feel".
 * Startup as launcher at boot fixes.
 * History supporting fixes.
* 1.2.5
 * Source code cleaned, now compiled without warnings.
 * Source code prepared to i18n.
 * Reading Lists (i.e. FAVs, LRUs) with full UTF8 support.
 * Writing Lists (i.e. FAVs, LRUs) now is more frequent.
 * Added "Power Off" functionality (in complement to "Reboot") and rewriting implementation.
* 1.2.4
 * Sony PRS-T1 support is added, some layout changes.
 * Optional return to main window after application starts from "All Applications" window.
* 1.2.3
 * Context menu items 'Open with ...' and 'Create intent ...' added. Note that these items can be disabled via configuration settings.
 * Search root can be comma separated directories list as well as a single directory.
* 1.2.2
 * All configured networks now are present in advanced screen.
* 1.2.1
 * New buttons: scroll up/down by big steps. Note that the step length is configurable.
 * New advanced information screen (marked with '#' character).
* 1.2.0
 * New directory navigation, visible scrollbar (see settings).
 * gl16 mode (eInk refresh mode) may be set now via settings.
* 1.1.3
 * Check HW and warn if it isn\'t NookSimpleTouch.
* 1.1.2
 * Associations settings allows now to configure explicit applications invocation  as well as general intent creation.
 * Obsolete 'how to call readers' setting removed.
* 1.1.1
 * New Cancel/Revert buttons in settings screen.
* 1.1.0
 * Task manager / battery manager - just tap on memory or battery status (bottom of the main screen, launcher mode only).