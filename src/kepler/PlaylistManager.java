/***************************************************
 * Copyright 2007 by Simran Gleason                *
 * This program is distributed under the terms     *
 * of the GNU General Public License.              *
 * See kepler.Kepler.LICENSE_TEXT or               *
 * http://www.gnu.org/licenses/gpl.txt             *
 ***************************************************/

package kepler;

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
public class PlaylistManager {
    private String title = "";
    private boolean shuffle = false;
    private boolean loop = false;
    private ArrayList playlists;
    private ArrayList shuffled = null;
    private int cursor = -1;
    private Playlist.Item onDeckItem = null;
    private Playlist.Item currentItem = null;
    private Playlist      currentPlaylist = null;

    public PlaylistManager() {
        playlists = new ArrayList();
    }

    public void reset() {
        cursor = -1;
        placeNextOnDeck();
    }

    public void addPlaylist(Playlist playlist) {
        playlists.add(playlist);
        playlist.setShuffle(shuffle);
        playlist.reset();
    }

    public int numPlaylists() {
        return playlists.size();
    }
    
    public ArrayList getPlaylists() {
        return playlists;
    }
    
    public void compactPlaylists() {
        ArrayList newPlaylists = new ArrayList();
        for(Iterator it = playlists.iterator(); it.hasNext();) {
            Playlist pl = (Playlist)it.next();
            if (pl.size() > 0) {
                newPlaylists.add(pl);
            }
        }
        this.playlists = newPlaylists;
    }

    public Playlist getOnDeckPlaylist() {
        Playlist result = getNthPlaylist(cursor);
        currentPlaylist = result;
        return result;
    }

    public Playlist peekOnDeckPlaylist() {
        return getNthPlaylist(cursor);
    }

    public Playlist getCurrentPlaylist() {
        return currentPlaylist;
    }

    public Playlist getNthPlaylist(int nth) {
        if (nth >= 0 && nth < playlists.size()) {
            return (Playlist)playlists.get(nth);
        } else {
            return null;
        }
    }

    public Playlist getPlaylistByTitle(String title) {
        int index = findPlaylistByTitle(title);
        return getNthPlaylist(index);
    }
        
    public int findPlaylistByTitle(String title) {
        if (title == null) {
            title = "";
        }
        int index = 0;
        for(Iterator it = playlists.iterator(); it.hasNext(); ) {
            Playlist pl = (Playlist)it.next();
            String playlistTitle = pl.getTitle();
            if (playlistTitle == null) {
                playlistTitle = "";
            }
            if (title.equals(playlistTitle)) {
                return index;
            }
            index++;
        }
        return -1;
    }
    
    public int findPlaylist(Playlist playlist) {
        int index = 0;
        for(Iterator it = playlists.iterator(); it.hasNext(); ) {
            Playlist candidate = (Playlist)it.next();
            if (playlist == candidate) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
        for(Iterator it = playlists.iterator(); it.hasNext(); ) {
            Playlist pl = (Playlist)it.next();
            pl.setShuffle(shuffle);
        }
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public boolean hasOnDeckItem() {
        return onDeckItem != null;
    }
    
    public Playlist.Item getOnDeckItem() {
        currentItem = onDeckItem;
        return onDeckItem;
    }

    public Playlist.Item peekOnDeckItem() {
        return onDeckItem;
    }

    public Playlist.Item getCurrentItem() {
        return currentItem;
    }

    public Playlist.Item getNextItem() {
        Playlist.Item result = onDeckItem;
        placeNextOnDeck();
        return result;
    }

    public void placeNextOnDeck() {
        Playlist onDeckPlaylist = getOnDeckPlaylist();
        Playlist.Item item = null;
        if (onDeckPlaylist != null && onDeckPlaylist.hasNext()) {
            item = onDeckPlaylist.next();
        } else {
            cursor++;
            onDeckPlaylist = getOnDeckPlaylist();
            if (onDeckPlaylist == null && loop) {
                cursor = 0;
                onDeckPlaylist = getOnDeckPlaylist();
            }

            if (onDeckPlaylist != null) {
                onDeckPlaylist.reset();
                item = onDeckPlaylist.next();
            }
        }
        onDeckItem = item;
    }

    public void placePrevOnDeck() {
        // TODO: finish this
        Playlist onDeckPlaylist = getOnDeckPlaylist();
        Playlist.Item item = null;
        if (onDeckPlaylist != null && onDeckPlaylist.hasPrev()) {
            item = onDeckPlaylist.prev();
        } else {
            cursor--;
            onDeckPlaylist = getOnDeckPlaylist();
            if (onDeckPlaylist == null && loop) {
                cursor = playlists.size() - 1;
                onDeckPlaylist = getOnDeckPlaylist();
            }

            if (onDeckPlaylist != null) {
                onDeckPlaylist.resetToEnd();
                item = onDeckPlaylist.prev();
            }
        }
        onDeckItem = item;
    }

    public void placePlaylistOnDeck(Playlist playlist) {
        int index = findPlaylist(playlist);
        playlist.reset();
        cursor = index;
        placeNextOnDeck();
    }

    public void placeItemOnDeck(Playlist.Item itemName) {
        String onDeckPlaylistName = getOnDeckPlaylist().getTitle();
        placeItemOnDeck(onDeckPlaylistName, itemName);
    }
    
    public void placeItemOnDeck(String playlistName, Playlist.Item item) {
        int playlistIndex = findPlaylistByTitle(playlistName);
        Playlist pl = getNthPlaylist(playlistIndex);
        pl.setItem(item);
        onDeckItem = item;
        cursor = playlistIndex;
    }

    public void placeItemOnDeckByName(String playlistName, String itemName) {
        int playlistIndex = findPlaylistByTitle(playlistName);
        Playlist pl = getNthPlaylist(playlistIndex);
        pl.setItemByName(itemName);
        onDeckItem = pl.current();
        cursor = playlistIndex;
    }

    public void placeItemOnDeckByFull(String itemFull) {
        Playlist pl = getCurrentPlaylist();
        pl.setItemByFull(itemFull);
        onDeckItem = pl.current();
    }

    public void print(StringBuffer buf, boolean showCurrent) {
        Playlist odp = getOnDeckPlaylist();
        String odpTitle = (odp == null ? "nullPL" : odp.getTitle());
        buf.append("PlaylistManager. ondeck=(" + odpTitle + " : " + onDeckItem + ")\n");
        for(Iterator it = playlists.iterator(); it.hasNext(); ) {
            Playlist pl = (Playlist)it.next();
            buf.append(pl.getTitle() + "\n");
            String indent = "    ";
            pl.print(buf, indent, showCurrent);
        }
    }

    public String print(boolean showCurrent) {
        StringBuffer buf = new StringBuffer();
        print(buf, showCurrent);
        return buf.toString();
    }
        
    public static void test1() {
        PlaylistManager mgr = new PlaylistManager();
        Playlist one = new Playlist();
        one.setTitle("one");
        mgr.addPlaylist(one);
        one.add("Fiorst");
        one.add("Second");
        one.add("Third");
        one.add("Fourth");
        one.add("Fifth");
        Playlist two = new Playlist();
        two.setTitle("two");
        mgr.addPlaylist(two);
        two.add("2.1");
        two.add("2.2");
        two.add("2.3");
        two.add("2.4");
        mgr.reset();

        System.out.println(mgr.print(true));
    }
    
    public static void test2() {
        PlaylistManager mgr = new PlaylistManager();
        Playlist one = new Playlist();
        one.setTitle("one");
        mgr.addPlaylist(one);
        one.add("Fiorst");
        one.add("Second");
        one.add("Third");
        one.add("Fourth");
        one.add("Fifth");
        Playlist two = new Playlist();
        two.setTitle("two");
        mgr.addPlaylist(two);
        two.add("2.1");
        two.add("2.2");
        two.add("2.3");
        two.add("2.4");
        mgr.reset();
        
        System.out.println(mgr.print(true));

        System.out.println("not looping...");
        for(int i=0; i < 15; i++) {
            Playlist.Item up = mgr.getOnDeckItem();
            Playlist odp = mgr.getOnDeckPlaylist();
            String odptitle = (odp == null?  " nullPL " : odp.getTitle());
            if (up != null) {
                mgr.placeNextOnDeck();
            } 
            System.out.println(i + ": " + odptitle + ": " + up);
        }
        mgr.reset();
        mgr.setLoop(true);
        System.out.println("looping...");
        for(int i=0; i < 15; i++) {
            Playlist.Item up = mgr.getOnDeckItem();
            Playlist odp = mgr.getOnDeckPlaylist();
            String odptitle = (odp == null?  " nullPL " : odp.getTitle());
            if (up != null) {
                mgr.placeNextOnDeck();
            } 
            System.out.println(i + ": " + odptitle + ": " + up);
        }

        System.out.println("\n\n going prev...");
        for(int i=0; i < 9; i++) {
            mgr.placePrevOnDeck();
            Playlist.Item up = mgr.getOnDeckItem();
            Playlist odp = mgr.getOnDeckPlaylist();
            String odptitle = (odp == null?  " nullPL " : odp.getTitle());
            System.out.println(i + ": " + odptitle + ": " + up);
        }

        System.out.println("\n\n place item by name ...");
        mgr.placeItemOnDeckByName("two", "2.3");
        for(int i=0; i < 9; i++) {
            Playlist.Item up = mgr.getOnDeckItem();
            Playlist odp = mgr.getOnDeckPlaylist();
            String odptitle = (odp == null?  " nullPL " : odp.getTitle());
            System.out.println(i + ": " + odptitle + ": " + up);
            mgr.placeNextOnDeck();
        }
    }
        
    public static void main(String[] args) {
        test2();
    }
    

}
    
    