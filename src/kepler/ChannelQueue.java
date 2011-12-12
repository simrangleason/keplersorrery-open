/***************************************************
 * Copyright 2007 by Simran Gleason                *
 * This program is distributed under the terms     *
 * of the GNU General Public License.              *
 * See kepler.Kepler.LICENSE_TEXT or               *
 * http://www.gnu.org/licenses/gpl.txt             *
 ***************************************************/

package kepler;

public class ChannelQueue {
    int [] q = null;
    int cursor;
    int top;
    int capacity;
    int size;
    public ChannelQueue(int capacity) {
        this.capacity = capacity;
        q = new int[capacity];
        cursor = 0;
        top = 0;
        size = 0;
    }

    public void nq(int val) {
        if (cursor >= capacity) {
            cursor = 0;
        }
        q[cursor] = val;
        cursor ++;
        size++;
    }

    public int dq() {
        int val = top();
        top ++;
        size--;
        if (top >= capacity) {
            top = 0;
        }
        return val;
    }
        

    public int top() {
        if (empty()) {
            return -1;
        }
        return q[top];
    }

    public int capacity() {
        return capacity;
    }
    
    public int size() {
        return size;
    }

    public boolean empty() {
        return size == 0;
    }

    public boolean full() {
        return size >= capacity;
    }

    public String priq() {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        for(int i=0; i < q.length; i++) {
            buf.append(q[i]);
            if (i < q.length - 1) {
                buf.append(", ");
            }
        }
        buf.append("]");
        return buf.toString();
    }
    
    public static void main(String [] args) {
        ChannelQueue q = new ChannelQueue(5);
        q.nq(1);
        System.out.println(" 1. cursor: " + q.cursor + " size: " + q.size() + " top: " + + q.top + " top(): " + q.top() + " empty: " + q.empty() + " full: " + q.full() + q.priq() );
        q.nq(2);
        System.out.println(" 2. cursor: " + q.cursor + " size: " + q.size() + " top: " + + q.top + " top(): " + q.top() + " empty: " + q.empty() + " full: " + q.full() + q.priq());
        q.nq(3);
        System.out.println(" 3. cursor: " + q.cursor + " size: " + q.size() + " top: " + + q.top + " top(): " + q.top() + " empty: " + q.empty() + " full: " + q.full() + q.priq());
        q.nq(4);
        System.out.println(" 4. cursor: " + q.cursor + " size: " + q.size() + " top: " + + q.top + " top(): " + q.top() + " empty: " + q.empty() + " full: " + q.full() + q.priq());
        q.nq(5);
        System.out.println(" 5. cursor: " + q.cursor + " size: " + q.size() + " top: " + + q.top + " top(): " + q.top() + " empty: " + q.empty() + " full: " + q.full() + q.priq());

        System.out.println("  dq: " + q.dq() + q.priq());
        System.out.println("  cursor: " + q.cursor + " size: " + q.size() + " top: " + + q.top + " top(): " + q.top() + " empty: " + q.empty() + " full: " + q.full() + q.priq());
        System.out.println("  dq: " + q.dq() + q.priq());
        System.out.println("  cursor: " + q.cursor + " size: " + q.size() + " top: " + + q.top + " top(): " + q.top() + " empty: " + q.empty() + " full: " + q.full() + q.priq());
        System.out.println("  dq: " + q.dq() + q.priq());
        System.out.println("  cursor: " + q.cursor + " size: " + q.size() + " top: " + + q.top + " top(): " + q.top() + " empty: " + q.empty() + " full: " + q.full() + q.priq());

        q.nq(6);
        System.out.println(" 6. cursor: " + q.cursor + " size: " + q.size() + " top: " + + q.top + " top(): " + q.top() + " empty: " + q.empty() + " full: " + q.full() + q.priq());
        q.nq(7);
        System.out.println(" 7. cursor: " + q.cursor + " size: " + q.size() + " top: " + + q.top + " top(): " + q.top() + " empty: " + q.empty() + " full: " + q.full() + q.priq());
        q.nq(8);
        System.out.println(" 8. cursor: " + q.cursor + " size: " + q.size() + " top: " + + q.top + " top(): " + q.top() + " empty: " + q.empty() + " full: " + q.full() + q.priq());
        System.out.println("  dq: " + q.dq() + q.priq());
        System.out.println("  cursor: " + q.cursor + " size: " + q.size() + " top: " + + q.top + " top(): " + q.top() + " empty: " + q.empty() + " full: " + q.full() + q.priq());
        System.out.println("  dq: " + q.dq() + q.priq());
        System.out.println("  cursor: " + q.cursor + " size: " + q.size() + " top: " + + q.top + " top(): " + q.top() + " empty: " + q.empty() + " full: " + q.full() + q.priq());
        System.out.println("  dq: " + q.dq() + q.priq());
        System.out.println("  cursor: " + q.cursor + " size: " + q.size() + " top: " + + q.top + " top(): " + q.top() + " empty: " + q.empty() + " full: " + q.full() + q.priq());
        System.out.println("  dq: " + q.dq() + q.priq());
        System.out.println("  cursor: " + q.cursor + " size: " + q.size() + " top: " + + q.top + " top(): " + q.top() + " empty: " + q.empty() + " full: " + q.full() + q.priq());
        System.out.println("  dq: " + q.dq() + q.priq());
        System.out.println("  cursor: " + q.cursor + " size: " + q.size() + " top: " + + q.top + " top(): " + q.top() + " empty: " + q.empty() + " full: " + q.full() + q.priq());
        System.out.println("  dq: " + q.dq() + q.priq());
        System.out.println("  cursor: " + q.cursor + " size: " + q.size() + " top: " + + q.top + " top(): " + q.top() + " empty: " + q.empty() + " full: " + q.full() + q.priq());

    }
}
