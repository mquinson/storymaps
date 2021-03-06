
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

import DragAndDrop.*;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import java.awt.Color;

/**
 * Base class for StoryMap and StoryCards.
 * 
 * @author seanh
 */
public class StoryBase implements DroppableOwner {

    protected PPath background;
        
    protected Droppable droppable;

    private CentreAlignedRowsLayoutNode layout;

    private PNode overlay;

    private Color color;

    public StoryBase(double width, double height, double xoffset,
            double yoffset, Color color, double margin_left,
            double margin_top, double spacing) {

        this.color = color;

        background = PPath.createRectangle(0, 0, (float)width, (float)height);
        background.setWidth(width);
        background.setHeight(height);
        background.setOffset(xoffset,yoffset);
        background.setPaint(color);
        background.setStrokePaint(color);

        layout  = new CentreAlignedRowsLayoutNode(spacing);
        double _width = background.getBounds().getWidth() - 2*margin_left;
        double _height = background.getBounds().getHeight() - 2*margin_top;
        layout.setBounds(0,0,_width,_height);
        layout.setOffset(margin_left,margin_top);
        background.addChild(layout);

        overlay = new PNode();
        overlay.setOffset(layout.getOffset());
        background.addChild(overlay);

        try {
            droppable = new Droppable(background,this);            
        } catch (NodeAlreadyDroppableException e) {
            // ...
        }                       
    }
    
    /**
     * A node has been dragged and dropped onto this story base. Just refuse
     * the drop. Subclasses should override this method.
     */
    public boolean dropped_onto(DropEvent de) {
        return false;
    }
    
    /**
     * Return the root node of this story base.
     */
    public PNode getNode() {
        return background;
    }

    public Color getColor() {
        return new Color(this.color.getRed(),this.color.getGreen(),
                this.color.getBlue());
    }

    /**
     * Add a node to the grid.
     */
    public void addToGrid(PNode node) {
        layout.addChild(node);
        layout.layoutChildren();
    }
    
    /**
     * Add a node to the overlay.
     */
    public void addToOverlay(PNode node) {
        overlay.addChild(node);
    }
}