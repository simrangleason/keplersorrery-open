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

public class Scale extends Playable {
    protected ArrayList notes;
    protected String name;
    public Scale(String name) {
        this.name = name;
        notes = new ArrayList();
    }

    public String getType() {
        return "scale";
    }

    // Playable api for getting the next note. 
    public Note nextNote(Body b, Orrery orrery) {
        int radialPercent = (int)(Math.min(100., b.getPos().magnitude() / orrery.radius * 100.));
        return getPercentNote(radialPercent);
    }

    public boolean atEnd()  {
        return false;
    }

    public Playable duplicate() {
        Scale clown = new Scale(getName());
        for(Iterator it=notes.iterator(); it.hasNext(); ) {
            clown.addNote((Note)it.next());
        }
        return clown;
    }

    public void reset() {
    }

    public void writeBody(StringBuffer buf) {
        for(Iterator it = notes.iterator(); it.hasNext();) {
            Note note = (Note)it.next();
            note.writeBody(buf);
            buf.append(" ");
        }
        buf.append("end");
    }

    public void writeBodyXML(StringBuffer buf) {
        for(Iterator it = notes.iterator(); it.hasNext();) {
            Note note = (Note)it.next();
            note.writeBody(buf);
            buf.append(" ");
        }
        // when writing the body out for XML, we don't want the 'end' marker
    }

    public void writeCurrentState(StringBuffer buf) {
        writeBody(buf);
    }

    ////////////////////////////////////////////
    
    public int length() {
        return notes.size();
    }

    public String getName() {
        return this.name;
    }
    public void setName(String val) {
        this.name = val;
    }

    

    public Note getNote(int i) {
        return (Note)notes.get(i);
    }
    public Note getPercentNote(int pct) {
        return getNote((int)(pct *  (length()-1) / 100));
    }

    public Iterator iterator() {
        return notes.iterator();
    }

    public void addNote(Note note) {
        notes.add(note);
    }

    public String toString() {
        return toString(-1);
    }
    public String toString(int highlight) {
        StringBuffer buf = new StringBuffer();
        toString(buf, highlight);
        return buf.toString();
    }

    public void toString(StringBuffer buf) {
        toString(buf, -1);
    }
    
    public void toString(StringBuffer buf, int highlight) {
        buf.append("Scale[");
        buf.append(name);
        buf.append("] {");
        int i=0;
        for(Iterator it = notes.iterator(); it.hasNext(); ) {
            Note note = (Note)it.next();
            if (highlight == i) {
                buf.append("<");
            }
            note.toString(buf);
            if (highlight == i) {
                buf.append(">");
            }
            if (it.hasNext()) {
                buf.append(" ");
            }
            i++;
        }
        buf.append("}");
    }
}
