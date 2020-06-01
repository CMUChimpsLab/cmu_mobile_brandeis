package edu.cmu.policymanager.viewmodel;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.cmu.policymanager.DataRepository.db.model.AppInfo;
import edu.cmu.policymanager.DataRepository.db.model.PermissionInfo;
import edu.cmu.policymanager.DataRepository.db.model.PurposeInfo;
import edu.cmu.policymanager.DataRepository.db.model.ThirdPartyLibInfo;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.ui.common.Searchable;

/**
 * Created by Mike Czapik (Carnegie Mellon University) on 5/21/2018.
 *
 * W4PGraph stores W4PData and the W4PData it is "connected" or "related" to.
 * For example, the root node may store a Who or app data. This is then linked to
 * the What or permissions it requests, which are its child/neighboring nodes. Each
 * permission has a Why or purpose for its request, and thus each permission node has its
 * own children.
 *
 * Examples:
 * app -> { permission1 -> { purpose1, purpose2 }, permission2 -> { purpose1 } }
 * purpose -> { app1 -> { permission1 }, app2 -> { permission1, permission2 } }
 *
 * W4PGraph can also be used to store lists of graphs. In this case, the root node's
 * children consists of other "root nodes" of apps or permissions and so on. This is
 * useful when visualizing sets of graphs, rather than just one (like in a config screen).
 *
 * Subgraphs of a W4PGraph can be referenced or cloned and passed to a UI component, where each
 * "level" of the graph is inspected and relevant values extracted. In some cases, you may
 * pass the entire graph.
 *
 * It is assumed that each activity that requests a W4PGraph knows in advance what the
 * structure should look like, because only that activity knows how to visualize its data.
 * It is therefore possible that the DataRepository does not return a W4PGraph that
 * fits what the activity wants to visualize. In that case, you will need to add a request
 * method to the DataRepository to get what you need.
 */

public final class W4PGraph implements Comparable<W4PGraph>, Searchable {
    private W4PData nodeData;
    private Map<String, String> metadata;
    private Set<W4PGraph> children;

    /**
     * Create completely empty graph
     * */
    public W4PGraph() {
        children = new TreeSet<W4PGraph>();
        metadata = new TreeMap<String, String>();
        nodeData = null;
    }

    /**
     * Create graph whose node value is W4PData
     *
     * @param data the W4PData (app/permission/purpose/library) to store
     * */
    public W4PGraph(W4PData data) {
        children = new TreeSet<W4PGraph>();
        metadata = new TreeMap<String, String>();
        nodeData = data;
    }

    /**
     * Create graph whose node value is W4PData, with metadata.
     *
     * @param data the W4PData (app/permission/purpose/library) to store
     * @param metadata metadata about this graph
     * */
    public W4PGraph(W4PData data,
                    Map<String, String> metadata) {
        children = new TreeSet<W4PGraph>();
        this.metadata = metadata;
        nodeData = data;
    }

    /**
     * Add a child node to this graph
     *
     * @param child the child node to add
     * */
    public void addChild(W4PGraph child) {
        children.add(child);
    }

    /**
     * Set metadata about this graph (number of purposes, descriptions etc)
     *
     * @param metadata key/value pairs of metadata about this graph
     * */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * Add a set of child nodes as graphs to this graph/node
     *
     * @param children the children to add
     * */
    public void addChildren(Set<W4PGraph> children) {
        this.children = children;
    }

    /**
     * Get the child nodes of this graph/node.
     * @return the child nodes
     * */
    public Set<W4PGraph> getChildren() { return children; }

    /**
     * Merges basically a linked list into this graph
     *
     * Ex: app -> perm -> purp -> lib
     *
     * @param graph the graph to merge into this one
     * */
    public void merge(W4PGraph graph) {
        navigateAndInsert(graph, this);
    }

    private void navigateAndInsert(W4PGraph toAdd,
                                   W4PGraph currentNode) {
        if(currentNode != null && toAdd != null) {
            final W4PGraph foundNode = currentNode.findInChildren(toAdd.getW4PData());

            if(foundNode != null) { navigateAndInsert(toAdd.getChild(), foundNode); }
            else { currentNode.addChild(toAdd); }
        }
    }

    private W4PGraph findInChildren(W4PData searchElement) {
        for(W4PGraph childElement : children) {
            if(childElement.getW4PData().equals(searchElement)) { return childElement; }
        }

        return null;
    }

    public W4PGraph getChild() {
        Iterator<W4PGraph> iterator = children.iterator();
        if(iterator.hasNext()) { return iterator.next(); }

        return null;
    }

    /**
     * Gets the W4PData stored at this graph/node
     *
     * @return the W4PData
     * */
    public W4PData getW4PData() { return nodeData; }
    public Map<String, String> getMetadata() { return metadata; }

    /**
     * Search for a graph node by its system name (package name, qualified permission name etc)
     *
     * @param systemName the system name to search for
     * @return the graph whose W4PData element matches the system name
     * */
    public W4PGraph getNodeBySystemName(String systemName) {
        Deque<W4PGraph> nodesToVisit = new ArrayDeque<W4PGraph>();

        nodesToVisit.push(this);
        nodesToVisit.addAll(children);

        while(!nodesToVisit.isEmpty()) {
            W4PGraph next = nodesToVisit.pop();

            if((next != null) && (next.getW4PData() != null)) {
                if (next.getW4PData().getAndroidSystemName().equals(systemName)) {
                    return next;
                }

                nodesToVisit.addAll(next.getChildren());
            }
        }

        return null;
    }

    public int compareTo(W4PGraph o) {
        if(o == null || o.children == null || children == null) { return 1; }

        for(W4PGraph v : children) {
            if (v.getW4PData().equals(o.getW4PData())) { return 0; }
        }

        return 1;
    }

    public String toString() {
        String graphAsString = "";

        if(nodeData != null) { graphAsString += nodeData.getAndroidSystemName(); }

        if((children != null) && (children.size() > 0)) {
            int i = 0;

            for(W4PGraph child : children) {
                if(i < children.size() - 1) {
                    graphAsString += " -> (" + child.toString() + "), ";
                }
                else {
                    graphAsString += " -> (" + child.toString() + ")";
                }

                i++;
            }
        }

        return graphAsString;
    }

    public boolean contains(final String[] keywords) {
        boolean hasWord = false;

        if(nodeData != null) {
            for(String word : keywords) {
                final String sysName = nodeData.getAndroidSystemName().toLowerCase();
                final String display = nodeData.getDisplayName().toLowerCase();

                if(sysName.contains(word.toLowerCase())) { return true; }
                if(display.contains(word.toLowerCase())) { return true; }
            }
        }

        if((children != null) && (children.size() > 0)) {
            for(W4PGraph child : children) {
                hasWord = child.contains(keywords);
                if(hasWord) { return hasWord; }
            }
        }

        return hasWord;
    }

    public boolean contains(final String keyword) {
        return contains(new String[] {keyword});
    }
}