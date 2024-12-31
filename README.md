This Widget can read multiple markdownfiles and display them on the homescreen.

Each file is seperated by a divider in the scroll list and the filenames are handled as headings.

At the moment it can read text, checkboxes with text and headings #, ##, ### (displayed all in the same font size(due to widget size limitations))

API 31, aka Android 12 is required else it wont work.

I have written this for my own use with Obsidian. Therefore the click on a task or text name will open Obsidian if installed.

At the moment onyl one Widget is supported.

The app was mainly created because other widgets did not support transparent background/text.




How to use:

Add it like any widget from the homescreen.
Then the config screen as shown will appear:

![Screenshot 2024-12-31 134840](https://github.com/user-attachments/assets/d44e9a3e-b8e7-4469-bf08-7d2bf08d16b3)

Here you have to select the path to the markdown files. In my case the Obsidian Vault I want to show:
![Screenshot 2024-12-31 134930](https://github.com/user-attachments/assets/f1807fcc-3fcd-4efd-a7a6-8ef44e571b5e)

Back in the config screen you can then change the order of the note files per drag and drop (press and hold then move upwards or downwards) and remove unwanted files per slide to the reigth or left.

You are also able to change the background (if you are not familar with ARGB or RGBA see: https://en.wikipedia.org/wiki/RGBA_color_model) and textcolor. Textcolor and Checkboxcolor will always be the same.

The value for the background is if not changed A:30 R:0 G:0 B:0

The value for the text&checkbox is if not changed A:80 R:255 G:255 B:255

You can also change the colors later if you open the "Notes app" (app of the widget).

Then press the Add Widget Button!

![Screenshot 2024-12-31 135003](https://github.com/user-attachments/assets/8b9aa139-c145-4579-9f26-97282a9fec59)

As this is not and official widget from Obsidian or any other markdown note app, you have to refresh the widget, with the refresh button on the top right side, after changing something in the selected App.

The trash button deletes all checked ( [x] ) tasks from the widget and the file

![Screenshot 2024-12-31 135430](https://github.com/user-attachments/assets/d6300e6f-544c-4d43-96bb-48044b0e3aef)



ToDos:
- Clean up messy code
- Implement different colors for text and checkbox
- Support multiple widgets
- Implement customizable font size
- Implement other markdown syntax
- Add comments



