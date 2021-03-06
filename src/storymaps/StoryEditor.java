
/* 
    Copyright: (c) 2006-2012 Sean Hammond <seanhammond@lavabit.com>

    This file is part of Storymaps.

    Storymaps is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Storymaps is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Storymaps.  If not, see <http://www.gnu.org/licenses/>.

*/
package storymaps;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.io.IOException;
import java.util.ArrayList;
import storymaps.ui.Fonts;
import java.util.logging.Logger;

/**
 *
 * @author seanh
 */
class StoryEditor implements Receiver {    
    
    // The root panel of the StoryEditor, to which everything else is added.
    private JPanel rootPanel;

    // The 'Write your Story!' button. It toggles between showing writeText and
    // planText when the editor is collapsed and uncollapsed.
    private JButton writeButton;
    private String writeText = "Write your Story!";
    private ImageIcon writeIcon;
    private String planText = "Go back to planning";
    private ImageIcon planIcon;

    // Whether or not the story editor is collapsed.
    private boolean collapsed = true;
    
    // The panel that collapses/uncollapses.
    private JPanel collapsiblePanel;
        
    // The panel that contains the FunctionEditors for each function in the
    // story. It contains a FunctionEditor panel for each Propp function in the
    // story, in a CardLayout. FunctionEditors are dynamically added and removed
    // as the story map is changed.    
    private CardLayout editorsLayout = new CardLayout();    
    private JPanel editorsPanel = new JPanel(editorsLayout);

    // The title of the story. TODO: put this in topToolBar.
    private AutoSelectingTextField title = new AutoSelectingTextField("Enter your story's title here.");
    
    // The JFrame in which this StoryEditor will be used.
    private JFrame frame;
            
    private JToolBar bottomToolBar;    

    StoryEditor (JFrame frame) {
        this.frame = frame;
        makeRootPanel();
    }
    
    /**
     * Make the root panel and call methods to make all the other parts.
     */
    private void makeRootPanel() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new BorderLayout());
        
        JToolBar topToolBar = makeTopToolBar();
        rootPanel.add(topToolBar,BorderLayout.NORTH);
        
        collapsiblePanel = new JPanel();
        collapsiblePanel.setVisible(false); // Starts off collapsed.
        collapsiblePanel.setLayout(new BorderLayout());
        rootPanel.add(collapsiblePanel,BorderLayout.CENTER);

        JPanel centralPanel = makeCentralPanel();
        collapsiblePanel.add(centralPanel,BorderLayout.CENTER);
                                        
        makeBottomToolBar();
        collapsiblePanel.add(bottomToolBar,BorderLayout.SOUTH);
        
        Messager.getMessager().accept("button clicked",this,null);
    }
    
    private JToolBar makeTopToolBar() {
        JToolBar topToolBar = new JToolBar();
        topToolBar.setFloatable(false);
        topToolBar.setRollover(true);        

        writeButton = new JButton();
        writeButton.setToolTipText("Click here to start writing your story.");
        writeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                collapse();
            }
        });
        try {
            writeIcon = Util.readImageIconFromClassPath("/data/icons/write.png");
            planIcon = Util.readImageIconFromClassPath("/data/icons/arrow_up.png");
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).warning("StoryEditor: IOException when reading in icons." + e.toString());
        }
        writeButton.setText(writeText);
        writeButton.setIcon(writeIcon);
        writeButton.setVerticalTextPosition(AbstractButton.CENTER);
        writeButton.setHorizontalTextPosition(AbstractButton.LEADING);
        topToolBar.add(writeButton);

        topToolBar.add(new JSeparator(SwingConstants.VERTICAL));
        
        title.setFont(Fonts.HUGE);
        title.setHorizontalAlignment(JTextField.CENTER);
        topToolBar.add(title);
                
        JButton sortButton = new JButton();
        sortButton.setToolTipText("Sort your story cards into the right order.");
        sortButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Messager.getMessager().send("sort", null);
            }
        });
        configureButton("Sort", "/data/icons/sort.png", sortButton);
        sortButton.setVerticalTextPosition(AbstractButton.CENTER);
        sortButton.setHorizontalTextPosition(AbstractButton.LEADING);
        topToolBar.add(sortButton);                
        
        return topToolBar;
    }
    
    private JButton makePrevButton() {
        JButton prev = new JButton("Prev");
        prev.setToolTipText("Go to the previous story card.");
        try {
            ImageIcon icon = Util.readImageIconFromClassPath("/data/icons/arrow_left.png");
            prev.setIcon(icon);
            prev.setVerticalTextPosition(AbstractButton.BOTTOM);
            prev.setHorizontalTextPosition(AbstractButton.CENTER);             
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).warning("IOException when reading icon "+e.toString());
        }
        prev.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editorsLayout.previous(editorsPanel);
            }
        });
        return prev;
    }
    
    private JButton makeNextButton() {
        JButton next = new JButton("Next");
        next.setToolTipText("Go to the next story card.");
        try {
            ImageIcon icon = Util.readImageIconFromClassPath("/data/icons/arrow_right.png");
            next.setIcon(icon);
            next.setVerticalTextPosition(AbstractButton.BOTTOM);
            next.setHorizontalTextPosition(AbstractButton.CENTER);             
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).warning("IOException when reading icon "+e.toString());
        }
        next.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editorsLayout.next(editorsPanel);
            }
        });
        return next;
    }
    
    private JPanel makePrevPanel() {
        JPanel prevPanel = new JPanel();
        prevPanel.setLayout(new BoxLayout(prevPanel,BoxLayout.Y_AXIS));
        JButton prevButton = makePrevButton();
        prevPanel.add(prevButton);
        return prevPanel;
    }
    
    private JPanel makeNextPanel() {
        JPanel nextPanel = new JPanel();
        nextPanel.setLayout(new BoxLayout(nextPanel,BoxLayout.Y_AXIS));

        nextPanel.add(Box.createVerticalGlue());
        
        JButton nextButton = makeNextButton();
        nextButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        nextPanel.add(nextButton);      
        
        nextPanel.add(Box.createVerticalGlue());
                        
        return nextPanel;
    }
    
    private JPanel makeCentralPanel() {
        JPanel centralPanel = new JPanel();
        centralPanel.setLayout(new BoxLayout(centralPanel,BoxLayout.X_AXIS));
        JPanel prevPanel = makePrevPanel();
        prevPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        centralPanel.add(prevPanel);
        editorsPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        editorsPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        centralPanel.add(editorsPanel);
        JPanel nextPanel = makeNextPanel();
        nextPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        centralPanel.add(nextPanel);
        return centralPanel;
    }
        
    private void configureButton(String text, String imagePath, JButton button) {
        button.setText(text);
        try {
            ImageIcon icon = Util.readImageIconFromClassPath(imagePath);
            button.setIcon(icon);
        } catch (IOException e) {
            // If we can't read an icon file we don't report it to the user,
            // just create a button with text and no icon.
        }        
        button.setVerticalTextPosition(AbstractButton.BOTTOM);
        button.setHorizontalTextPosition(AbstractButton.CENTER);        
    }
    
    private JButton addButton(String text, String imagePath, JToolBar toolBar) {
        JButton button = new JButton();
        configureButton(text, imagePath, button);
        toolBar.add(button);
        return button;
    }
    
    private JButton addButton(String text, String imagePath, Action action, JToolBar toolBar) {
        JButton button = new JButton(action);
        configureButton(text, imagePath, button);
        toolBar.add(button);
        return button;
    }
    
    private void makeBottomToolBar() {        
        bottomToolBar = new JToolBar();
        bottomToolBar.setFloatable(false);
        bottomToolBar.setRollover(true);

        addButton("Cut","/data/icons/cut.png",new DefaultEditorKit.CutAction(),bottomToolBar);
        addButton("Copy","/data/icons/copy.png",new DefaultEditorKit.CopyAction(),bottomToolBar);
        addButton("Paste","/data/icons/paste.png",new DefaultEditorKit.PasteAction(),bottomToolBar);
        
        bottomToolBar.add(new JSeparator(SwingConstants.VERTICAL));

        JButton saveButton = addButton("Save Story","/data/icons/save.png",bottomToolBar);
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Application.getInstance().save();
            }
        });
        JButton previewButton = addButton("Preview Story","/data/icons/preview.png",bottomToolBar);
        previewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Application.getInstance().preview();
            }
        });
        JButton exportButton = addButton("Save Story as HTML", "/data/icons/save_as_html.png",bottomToolBar);
        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Application.getInstance().saveAsHTML();
            }
        });
        JButton printButton = addButton("Print Story","/data/icons/print.png",bottomToolBar);
        printButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Application.getInstance().print();
            }
        });
    }
                
    private void collapse() {
        if (collapsed) {
            collapsed = false;
            collapsiblePanel.setPreferredSize(new Dimension(frame.getWidth(),275 + bottomToolBar.getHeight()));
            collapsiblePanel.setVisible(true);
            collapsiblePanel.getParent().validate();
            collapsiblePanel.getParent().repaint();
            writeButton.setText(planText);
            writeButton.setIcon(planIcon);
            Messager.getMessager().send("Editor uncollapsed", this);
        } else {
            collapsed = true;
            collapsiblePanel.setPreferredSize(new Dimension(frame.getWidth(),0));
            collapsiblePanel.setVisible(false);
            collapsiblePanel.getParent().validate();
            collapsiblePanel.getParent().repaint();
            writeButton.setText(writeText);
            writeButton.setIcon(writeIcon);
            Messager.getMessager().send("Editor collapsed", this);
        }
    }
    
    /**
     * Update the list of FunctionEditors in this StoryEditor.
     */
    public void update(ArrayList<StoryCard> new_cards) {
        editorsPanel.removeAll();
        for (StoryCard s : new_cards) {
            FunctionEditor e = s.getEditor();
            // FIXME: this might cause a problem if we can have two story cards
            // with the same name.
            editorsPanel.add(e.getComponent(),e.getFunction().getName());
        }
    }
        
    /**
     * Return the root component of this StoryEditor, i.e. the component that
     * should be added to a contentPane to add this StoryEditor to a JFrame.
     */
    public JComponent getComponent() {
        return rootPanel;
    }
            
    public boolean isCollapsed() {
        return collapsed;
    }
    
    public String getTitle() {
        return title.getText();
    }
    
    public void setTitle(String title) {
        this.title.setText(title);
    }

    public void receive(String name, Object receiver_arg, Object sender_arg) {
        if (name.equals("button clicked")) {
            if ( ((String)sender_arg).equals("Write Story") ) {
                collapse();
            }
        }
    }
}