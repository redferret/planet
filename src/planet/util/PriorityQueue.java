
package planet.util;


/**
 * Priority Queue uses threads as a means to convey concept of priorities.
 * Threads do not run. When a new thread of priorities p is added to the queue
 * the enqueue method will sequentially search, starting from the back of the
 * queue and compare priorities. If the new thread has a higher priority it will
 * preempt the smaller priority but it will increase the lower priority by 1. If
 * the priorities are equal then the new node is placed before the node of equal
 * or less priorities.
 *
 * @author Richard DeSilvey
 * @param <I>
 */
public class PriorityQueue<I> {

    private class Node {
        private I item;
        private float priority;
        private Node next;
        private Node prev;

        Node(Node prev, I element, Node next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
            priority = -1;
        }
        
        public float getPriority(){
            return priority;
        }
        
        public void setPriority(float p){
            priority = p;
        }
       
        public String toString(){
            return item.toString();
        }
    }
    
    private Node first, last;
    private int size;
    
    public PriorityQueue(){
        size = 0;
        first = last = null;
    }
    
    private void placeBefore(Node before, I task, float priority) {

        final Node pred = before.prev;
        final Node newNode = new Node(pred, task, before);
        newNode.setPriority(priority);
        before.prev = newNode;
        if (pred == null) {
            first = newNode;
        } else {
            pred.next = newNode;
        }
        size++;
//        System.out.println(toString());
    }
    
    private void putLast(I task, float priority) {
        final Node l = last;
        final Node newNode = new Node(l, task, null);
        newNode.setPriority(priority);
        last = newNode;
        
        if (l == null) {
            first = newNode;
        } else {
            l.next = newNode;
        }
        size++;
//        System.out.println(toString());
    }
    
    public void enqueue(I task, float priority){

        Node cur;
        
        cur = first;
        while(cur != null){
            if (priority < cur.getPriority()){
                cur.setPriority(Math.max(0, cur.getPriority() - 1f));
            }else {
                placeBefore(cur, task, priority);
                return;
            }
            cur = cur.next;
        }
        putLast(task, priority);
    }
    
    public I dequeue(){
        final I element = last.item;
        final Node prev = last.prev;
        last.item = null;
        last.prev = null;
        
        last = prev;
        if (prev == null)
            first = null;
        else
            prev.next = null;
        size--;
        
        return element;
    }
    
    public boolean isEmpty(){
        return size == 0;
    }
    
    public int size(){
        return size;
    }
    
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Node cur = first;
        
        while(cur != null){
            sb.append(cur.toString()).append(", ");
            cur = cur.next;
        }
        sb.append("\n");
        return sb.toString();
    }
    
    public String printSimple() {
        StringBuilder sb = new StringBuilder();
        Node cur = first;

        while (cur != null) {
            sb.append(cur.item.toString()).append("-")
                    .append(":").append(cur.getPriority())
                    .append(", ");
            cur = cur.next;
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * At 100,000 elements, time was about 35 seconds with no dequeue.
     *
     * @param args
     */
    public static void main(String[] args) {

        PriorityQueue<String> queue = new PriorityQueue();

        queue.enqueue("A", 15);//3
        System.out.println(queue.printSimple());
        queue.enqueue("B", 10);//2
        System.out.println(queue.printSimple());
        queue.enqueue("C", 20);//4
        System.out.println(queue.printSimple());
        queue.enqueue("D", 5);//1
        System.out.println(queue.printSimple());
        queue.enqueue("E", 7);//1
        System.out.println(queue.printSimple());
        queue.enqueue("F", 7);//1
        System.out.println(queue.printSimple());
        queue.enqueue("G", 7);//1
        
        System.out.println(queue.printSimple());
    }

}
