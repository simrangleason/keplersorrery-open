/***************************************************
 * Copyright 2007 by Simran Gleason                *
 * This program is distributed under the terms     *
 * of the GNU General Public License.              *
 * See kepler.Kepler.LICENSE_TEXT or               *
 * http://www.gnu.org/licenses/gpl.txt             *
 ***************************************************/

package kepler;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Playlist implements a list of world files to be played.
 *
 * Supports returning the list in original order,
 * a getNext() operator that returns the files sequentially or
 * shuffled, with no repeats.
 *
 * Looping (i.e. returning to the beginning or another shuffle set after
 * running through the list in shuffled order) is to be handled by
 * the clients of the playlist.
 */
public class Playlist {

    private String prefix = "";
    private String title = "";
    private boolean shuffle = false;
    private ArrayList items;
    private ArrayList shuffled = null;

    private int cursor;
    
    public Playlist() {
        this.items = new ArrayList();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String val) {
        title = val;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String val) {
        prefix = val;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
        if (shuffle) {
            reshuffle();
        }
    }

    public void add(String name) {
        String full = this.prefix + name;
        items.add(new Item(this.prefix, name, full));
        if (shuffle) {
            reshuffle();
        }
    }

    public void add(String name, String full) {
        items.add(new Item(this.prefix, name, full));
        if (shuffle) {
            reshuffle();
        }
    }

    public void add(Item item) {
        items.add(item);
        if (shuffle) {
            reshuffle();
        }
    }


    public ArrayList getItems() {
        ArrayList list;
        if (shuffle) {
            list = shuffled;
        } else {
            list = items;
        }
        return list;
    }

    public ArrayList getShuffledItems() {
        return shuffled;
    }

    public ArrayList getUnshuffledItems() {
        return items;
    }

    

    public void reset() {
        cursor = -1;
        if (shuffle) {
            reshuffle();
        }
    }

    public void resetToEnd() {
        cursor = items.size();
    }

    public int size() {
        return items.size();
    }

    public boolean hasNext() {
        return items.size() > 0 && (cursor + 1) < items.size();
    }

    public boolean hasPrev() {
        return items.size() > 0 && cursor > 0;
    }
    
    public Item next() {
        Item result = null;
        if (hasNext()) {
            cursor++;
            if (cursor < items.size()) {
                result = getNth(cursor);
            }
        }
        return result;
    }
    
    public Item prev() {
        Item result = null;
        if (hasPrev()) {
            cursor--;
            if (cursor >= 0) {
                result = getNth(cursor);
            }
        }
        return result;
    }

    public Item current() {
        return getNth(cursor);
    }
    
    public Item getNth(int nth) {
        Item result = null;
        if (nth >=0 && nth < items.size()) {
            if (shuffle) {
                result = (Item)shuffled.get(cursor);
            } else {
                result = (Item)items.get(cursor);
            }
        }
        return result;
    }

    public Item setNth(int nth) {
        Item result = null;
        if (nth >=0 && nth < items.size()) {
            cursor = nth;
            result = getNth(nth);
        }
        return result;
    }

    public void setItem(Item item) {
        int index = findItem(item);
        if (index >= 0) {
            cursor = index;
        }
    }

    public void setItemByName(String name) {
        int index = findItemByName(name);
        if (index >= 0) {
            cursor = index;
        }
    }

    public void setItemByFull(String full) {
        int index = findItemByFull(full);
        if (index >= 0) {
            cursor = index;
        }
    }

    public int findItemByName(String name) {
        int index = 0;
        ArrayList list;
        if (shuffle) {
            list = shuffled;
        } else {
            list = items;
        }
        for(Iterator in = list.iterator(); in.hasNext(); ) {
            Item candidate = (Item)in.next();
            if (candidate.getName().equals(name)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public int findItemByFull(String full) {
        int index = 0;
        ArrayList list;
        if (shuffle) {
            list = shuffled;
        } else {
            list = items;
        }
        for(Iterator in = list.iterator(); in.hasNext(); ) {
            Item candidate = (Item)in.next();
            if (candidate.getFull().equals(full)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public int findItem(Item item) {
        int index = 0;
        ArrayList list;
        if (shuffle) {
            list = shuffled;
        } else {
            list = items;
        }
        for(Iterator in = list.iterator(); in.hasNext(); ) {
            Item candidate = (Item)in.next();
            if (candidate.equals(item)) {
                return index;
            }
            index++;
        }
        return -1;
    }
            
    private void reshuffle() {
        shuffled = scramble(items);
    }
    
    private ArrayList scramble(ArrayList list) {
        ArrayList unscrambled = new ArrayList();
        for(Iterator in = list.iterator(); in.hasNext(); ) {
            unscrambled.add(in.next());
        }
        ArrayList scrambled = new ArrayList();
        int n = unscrambled.size();
        for(int i=0; i < n; i++) {
            int place = (int)(n * Math.random());
            Item item = (Item)unscrambled.get(place);
            while (item == null) {
                place ++;
                if (place >= n) {
                    place = 0;
                }
                item = (Item)unscrambled.get(place);
            }
            scrambled.add(item);
            unscrambled.set(place, null);
        }
        return scrambled;
    }

    public void print(StringBuffer buf, String indent, boolean showCurrent) {
        int index = 0;
        ArrayList list;
        if (shuffle) {
            list = shuffled;
        } else {
            list = items;
        }
        for(Iterator it = list.iterator(); it.hasNext(); ) {
            Item item = (Item)it.next();
            buf.append(indent);
            if (showCurrent && index == cursor) {
                buf.append("[");
                buf.append(item.getName());
                buf.append("]");
            } else {
                buf.append(item.getName());
            }
            buf.append("\n");
            index++;
        }
    }

    class Item {
        private String prefix;
        private String name;
        private String full;
        private int lengthLimit = 12;
        public Item(String prefix, String name, String full) {
            this.prefix = prefix;
            this.name = name;
            this.full = full;
        }

        public String getName() {
            return name;
        }

        public String getFull() {
            return full;
        }
        
        public String getShortName() {
            return limitChars(name, lengthLimit);
        }

        public String getShortName(int limit) {
            return limitChars(name, limit);
        }
        
        public String toString() {
            return getShortName(); // TODO: limit length
        }
        
     }
    
    public static String limitChars(String str, int limit) {
        if (str.length() > limit) {
            return str.substring(0, limit);
        }
        return str;
    }
    
    public static String extractName(String path) {
        File file = new File(path);
        return file.getName();
    }

}