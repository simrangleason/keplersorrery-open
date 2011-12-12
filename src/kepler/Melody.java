/***************************************************
 * Copyright 2007 by Simran Gleason                *
 * This program is distributed under the terms     *
 * of the GNU General Public License.              *
 * See kepler.Kepler.LICENSE_TEXT or               *
 * http://www.gnu.org/licenses/gpl.txt             *
 ***************************************************/

package kepler;

import java.util.Iterator;

public class Melody extends  Scale {
    protected int cursor = -1;
    protected boolean wrap = true;
    protected Scale scale = null;

    public Melody(Scale sc) {
        super(sc.getName());
        this.scale = sc;
        this.cursor = 0;
    }

    public String getType() {
        return "melody";
    }

    public Scale getScale() {
        return scale;
    }
    
    public Playable duplicate() {
        return new Melody((Scale)this.scale.duplicate());
    }
    
    // Playable api for getting the next note. 
    public Note nextNote(Body b, Orrery orrery) {
        return next();
    }
    public boolean atEnd() {
        return (cursor >= length());
    }


    public void reset() {
        reset(0);
    }

    public void writeBody(StringBuffer buf) {
        scale.writeBody(buf);
    }

    public void writeBodyXML(StringBuffer buf) {
        scale.writeBodyXML(buf);
    }
    
    public void writeCurrentState(StringBuffer buf) {
        int i = 0;
        for(Iterator it = scale.notes.iterator(); it.hasNext();) {
            Note note = (Note)it.next();
            boolean currentNote = false;
            if (cursor == i) {
                currentNote = true;
            }
            if (currentNote) {
                buf.append("[");
            }
            note.writeBody(buf);
            if (currentNote) {
                buf.append("]");
            }
            buf.append(" ");
            i++;
        }
        buf.append("end");
    }

    ///////////////////////////////
    
    public void reset(int nth) {
        cursor = nth;
    }

    public int length() {
        return scale.length();
    }

    public Note next() {
        if (cursor == -1) {
            cursor = 0;
        }
        int len = length();
        if (cursor >= len ) {
            cursor -= len;
        }
        Note note = scale.getNote(cursor);
        cursor ++;
        return note;
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
        buf.append("Melody[");
        buf.append(name);
        buf.append("] {");
        int i=0;
        for(Iterator it = scale.notes.iterator(); it.hasNext(); ) {
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
