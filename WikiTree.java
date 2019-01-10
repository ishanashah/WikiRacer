import java.util.*;

/*
WikiTree is a tree structure that abstracts Wikipedia pages and their links to other pages.
Wikipedia can be represented as a graph, where each page is a node and has directed edges
towards every page it is linked to. Since we only need to find a directed path from one page
to another, we don't need to abstract an entire graph, but only a tree rooted at the start page.

The tree is initialized with the start page as the root. Since the root is a leaf, it is added
to the priority queue of leaves (which prefers pages with more links in common with the destination).
The best leaf is removed from the queue, and each of its children (pages in common with the destination)
are processed and added to the queue. The process is repeated until we find a leaf that has a link to
the destination.
 */
class WikiTree {
    //Set of all visited pages
    private Set<WikiPage> visited;

    //Priority Queue of all leaves to search from
    //Leaves are ordered such that pages with more
    //links in common with the destination are preferred
    private PriorityQueue<WikiNode> leaves;

    //Destination page
    private WikiPage destination;

    //Node that links to the destination page
    private WikiNode path;

    WikiTree(WikiPage start, WikiPage destination) {
        this.destination = destination;
        destination.parse();

        WikiNode root = new WikiNode(start);
        root.parse();

        //Initialize visited
        visited = new HashSet<>();
        visited.add(start);

        //Initialize leaves
        leaves = new PriorityQueue<>((WikiNode lhs, WikiNode rhs) ->
                rhs.getChildren().length - lhs.getChildren().length);
        leaves.add(root);
    }

    //Find a path to the destination and assign it to "path"
    //"path" is null if no path is found
    void search() {
        search: while(!leaves.isEmpty()) {
            //There are remaining potential search paths to process

            //The best leaf in the queue
            //(The leaf with the most links in common with the destination)
            WikiNode bestLeaf = leaves.poll();

            if(bestLeaf.getPage().linkedPages().contains(destination)) {
                //We have found a path to the destination
                path = bestLeaf;
                return;
            }

            if(bestLeaf.getChildren().length == 0) {
                //The best leaf has no links in common with the destination

                for(WikiPage neighbor: bestLeaf.getPage().linkedPages()) {
                    //Iterate over every page linked to by the best leaf

                    if(visited.contains(neighbor)) {
                        //Skip pages that have been visited
                        continue;
                    }
                    //Add this page to potential search paths
                    WikiNode nextLeaf = new WikiNode(neighbor);
                    nextLeaf.parse();
                    bestLeaf.addChild(nextLeaf);
                    leaves.add(nextLeaf);
                    visited.add(neighbor);
                    if(nextLeaf.getChildren().length != 0) {
                        //This page has pages in common with the destination
                        //Go to the start of the loop and process this page
                        //Skip the rest of the pages linked by the current leaf
                        //The processing of this path is left incomplete
                        //leaves.add(bestLeaf);
                        continue search;
                    }
                }
                //No page on the current leaf has any pages in common with
                //the destination
                //Give up the search
                //(Can't find a path in reasonable time)
                return;
            }

            //The best leaf has links in common with the destination
            for(WikiNode child: bestLeaf.getChildren()) {
                //Iterate over every page shared by best leaf and destination
                if(visited.contains(child.getPage())) {
                    //Skip pages that have been visited
                    continue;
                }
                //Add current path to the queue
                child.parse();
                leaves.add(child);
                visited.add(child.getPage());
            }
        }
    }

    //Print a list of pages to follow from the start
    //to reach the destination by using parent pointers
    //from the "path" field
    @Override
    public String toString() {
        if(path == null) {
            return "no results...\n";
        }

        Stack<WikiNode> parentStack = new Stack<>();
        for(WikiNode current = path; current != null;) {
            parentStack.push(current);
            current = current.getParent();
        }

        StringBuilder s = new StringBuilder();
        while(!parentStack.isEmpty()) {
            s.append(parentStack.pop());
            s.append(" >\n");
        }
        s.append(destination);
        s.append('\n');
        return s.toString();
    }

    /*
    WikiNode represents a Wikipedia page in the context of WikiTree.
    Every WikiNode must contain a reference to the page it represents.
    The children array is a list of WIkiNodes that the current node has
    in common with the destination. The length of this array is used in
    the leaves priority queue to determine which leaf should be processed
    next.

    A WikiNode must be parsed before it can be added to the leaves priority
    queue. Parsing a WikiNode initializes the children array.

    After being parsed, a WikiNode is technically no longer a leaf, since
    it now has children. Since none of the children have been parsed, we
    don't know what pages they have in common with the destination, so they
    cannot be added to the leaves queue. In essence, a "leaf" is not a node
    that has no children, but is rather a node whose children have not
    already been parsed.
     */
    private class WikiNode {
        //The page that this page was linked to by
        //null if this node is the starting page
        private WikiNode parent;

        //List of nodes for every page in common with destination
        private WikiNode[] children;

        //Reference to the page this node represents
        private WikiPage page;

        WikiNode(WikiPage page) {
            this.page = page;
        }

        //Initialize children
        private void parse() {
            if(children != null) {
                return;
            }
            page.parse();
            List<WikiNode> childrenList = new LinkedList<>();
            for(WikiPage p: page.linkedPages()) {
                if(destination.linkedPages().contains(p)) {
                    WikiNode child = new WikiNode(p);
                    childrenList.add(child);
                    child.setParent(this);
                }
            }
            children = childrenList.toArray(new WikiNode[0]);
        }

        private void setParent(WikiNode parent) {
            this.parent = parent;
        }

        private WikiNode getParent() {
            return parent;
        }

        private void addChild(WikiNode child) {
            child.setParent(this);
        }

        private WikiNode[] getChildren() {
            return children;
        }

        private WikiPage getPage() {
            return page;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof WikiNode &&
                    page.equals(((WikiNode) other).getPage());
        }

        @Override
        public int hashCode() {
            return page.hashCode();
        }

        @Override
        public String toString() {
            return page.toString();
        }
    }
}
