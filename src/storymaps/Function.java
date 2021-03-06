
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

import com.google.gson.*;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * A Function is a simple immutable object that represents one of Propp's
 * functions.
 * 
 * The Function class has a public static member functions that is a list of all
 * functions read in from the functions.xml file by XStream.
 * 
 * Function objects are also created when saved stories are read in from file,
 * so it is possible to have more than one Function object with the same fields,
 * or even with some fields the same but others different.
 * 
 * The Function class constructor is package-private so that FunctionConverter
 * can use it, but it should not be used otherwise.
 * 
 * @author seanh
 */
final class Function implements Comparable, Originator {

    /**
     * The number that defines the order of this function relative to other
     * functions, and that uniquely identifies this function. Used to implement
     * equals and comparable.
     */
    private final int number;
    
    /**
     * One or two word title for this function. Plain text.
     */
    private final String name;
    
    /**
     * Short one sentence description of this function. HTML-formatted.
     */
    private final String description;
    
    /**
     * Longer (multi-paragraph) structured instructions for writing this
     * function. Includes lists of options or examples where appropriate.
     * HTML-formatted.
     */
    private final String instructions;

    private final String imageFilename;
    private final String imagePath;
    private final Image image;
    private final String highDetailImagePath;
    private Image highDetailImage;
    
    /**
     * A singleton list containing a Function object for every function
     * represented in the functions.xml file.
     */
    private static List<Function> functions = null;
    
    private static void initialiseFunctionsIfNecessary() {
        if (functions == null) {
            try {
                String jsonString = Util.readTextFileFromClassPath("/data/functions/functions.json");
                // Use to debug the functions.json script when running in the source code:
                // String jsonString = Util.readTextFileFromSystem("src/data/functions/functions.json");
                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(jsonString);
                JsonArray jsonArray = element.getAsJsonArray();

                functions = new ArrayList<Function>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    if ( ! jsonArray.get(i).isJsonNull() ) {
                        try {
                            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                            int number = jsonObject.get("number").getAsInt();
                            String name = jsonObject.get("name").toString();
                            String description = jsonObject.get("description").toString();
                            String instructions = jsonObject.get("instructions").toString();
                            Function function = new Function(number,name,description,instructions);
                            functions.add(function);
                        } catch (IllegalStateException e) {
                            throw new RuntimeException("Exception when reading function #"+i+" in functions file.",e);
                        }
                    }
                }
            } catch (IOException e) {
                // If we can't read the functions file then the application
                // can't work.
                throw new RuntimeException("Could not read functions.json file.",e);
            } catch (JsonParseException e) {
                throw new RuntimeException("Exception when reading functions file.",e);
            }
        }
    }
    
    static List<Function> getFunctions() {
        initialiseFunctionsIfNecessary();
        return functions;
    }
    
    Function(int number, String name, String description, String instructions) {
        this.number = number;
        this.name = name;
        this.description = description;
        this.instructions = instructions;
        this.imageFilename  = number+".png";
        this.imagePath = "/data/functions/"+imageFilename;
        this.highDetailImagePath = "/data/functions/"+number+"-detail.png";
        try {
            this.image = Util.readImageFromClassPath(imagePath);
        } catch (IOException e) {
            System.out.println("Couldn't load image for function "+imagePath);
            // FIXME: shouldn't need to crash here.
            throw new RuntimeException("Couldn't load image for function "+imagePath,e);
        }
        try {
            this.highDetailImage = Util.readImageFromClassPath(highDetailImagePath);
        } catch (IOException e) {
            System.out.println("Couldn't load image for function "+imagePath);
            this.highDetailImage = this.image;
        }
    }
        
    public int getNumber() { return number; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getInstructions() { return instructions; }
    public Image getImage() { return image; }
    public String getImageFilename() { return imageFilename; }
    public String getImagePath() { return imagePath; }
    public Image getHighDetailImage() { return highDetailImage; }
    public String getHighDetailImagePath() { return highDetailImagePath; }
    
    @Override
    public String toString() {
        return "Function: "+number+", "+name;
    }
    
    /**
     * Return true if obj is equivalent to this function, false otherwise.
     * 
     * (This is in place of overriding equals, which is a PITA.)
     */
    public boolean compare(Object obj) {
        if (!(obj instanceof Function)) {
            return false;
        } else {
            Function f = (Function) obj;
            // A function's symbol is supposed to uniquely identify that
            // function, so we just check if the symbol's are the same.
            return f.number == this.number;
        }        
    }
    
    /**
     * Implement Comparable.
     */
    public int compareTo(Object arg) {
        Function other = (Function) arg;
        if (other.number > this.number) {
            return 1;
        } else if (other.number == this.number) {
            return 0;
        }
        return -1;
    }
    
    // Implement Originator
    // --------------------
    
    private static final class FunctionMemento implements Memento {
        // No need to defensively copy anything as int is a primitive type
        // and strings are immutable.
        private final int number;
        private final String name;
        private final String description;
        private final String instructions;
        FunctionMemento (Function f) {
            this.number = f.getNumber();
            this.name = f.getName();
            this.description = f.getDescription();
            this.instructions = f.getInstructions();
        }
        int getNumber() { return number; }
        String getName() { return name; }
        String getDescription() { return description; }
        String getInstructions() { return instructions; }
    }
    
    public Memento createMemento() {
        return new FunctionMemento(this);
    }
    
    public static Function newInstanceFromMemento(Memento m) throws MementoException {
        if (m == null) {
            String detail = "Null memento object.";
            MementoException e = new MementoException(detail);
            Logger.getLogger(Function.class.getName()).throwing("Function", "newInstanceFromMemento", e);
            throw e;
        }
        if (!(m instanceof FunctionMemento)) {
            String detail = "Wrong type of memento object.";
            MementoException e = new MementoException(detail);
            Logger.getLogger(Function.class.getName()).throwing("Function", "newInstanceFromMemento", e);
            throw e;
        }
        FunctionMemento f = (FunctionMemento) m;
        return new Function(f.getNumber(),f.getName(),f.getDescription(),
                f.getInstructions());
    }
}

