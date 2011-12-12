/***************************************************
 * Copyright 2007 by Simran Gleason                *
 * This program is distributed under the terms     *
 * of the GNU General Public License.              *
 * See kepler.Kepler.LICENSE_TEXT or               *
 * http://www.gnu.org/licenses/gpl.txt             *
 ***************************************************/

package kepler;

public abstract class Playable  {
    public abstract String   getName();
    public abstract String   getType();
    public abstract Note     nextNote(Body b, Orrery orrery);
    public abstract boolean  atEnd();
    public abstract Playable duplicate();
    public abstract void     reset();
    public abstract void     writeBody(StringBuffer buf);
    public abstract void     writeCurrentState(StringBuffer buf);

    boolean shared = false;
    public boolean isShared() {
        return shared;
    }
    public void setShared(boolean val) {
        this.shared = val;
    }

    public void write(StringBuffer buf) {
        buf.append(getType());
        buf.append(" ");
        buf.append(getName());
        buf.append(" ");
        writeBody(buf);
    }

    public void writeXML(String indent, StringBuffer buf) {
        buf.append(indent);
        buf.append("<");
        buf.append(getType());
        buf.append(" name=\"");
        buf.append(getName());
        buf.append("\">");
        writeBodyXML(indent, buf);
        buf.append("</");
        buf.append(getType());
        buf.append(">\n");
    }

    public void writeBodyXML(String indent, StringBuffer buf) {
        // defaults to calling writeBody(buf) -- i.e. no xml body content. 
        writeBody(buf);
    }
}
