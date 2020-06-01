package edu.cmu.policymanager.ui.common.datastructures;

import android.util.ArraySet;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Tree data structure whose node values contain state objects. Updating a tree node
 * triggers an update process throughout the tree to ensure that state is consistent.
 *
 * All child nodes will assume the value of their parent node when its state changes. The
 * node whose state triggered the updates will send a message to its parent node. The parent
 * node will then check the value of all of its children. If all child nodes are the same value,
 * then the parent's state will change and begin the process again.
 *
 * This kind of tree is necessary to keep track of state for policy controls, and to ensure
 * that parent controls assume the same value as their children when their value changes.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class ConsistentStateTree {
    private State mState;
    private ConsistentStateTree mParentNode;
    private List<ConsistentStateTree> mChildNodes;

    /**
     * Creates a ConsistentStateTree instance with the given State.
     *
     * @param state the State object this tree will hold.
     * */
    public ConsistentStateTree(State state) {
        mState = state;
        mParentNode = null;
        mChildNodes = new LinkedList<ConsistentStateTree>();

        mState.setReferenceToContainingTree(this);
    }

    /**
     * Tests for equality between two ConsistentStateTrees. Equality is determined by
     * checking the state value of this ConsistentStateTree node to the one provided. No
     * searching is done.
     *
     * @param o the ConsistentStateTree to compare against.
     * @return result of equality test
     * */
    public boolean equals(Object o) {
        ConsistentStateTree other = (ConsistentStateTree)o;
        return mState.equals(other.mState);
    }

    /**
     * Adds a child ConsistentStateTree node, or entire subtree.
     *
     * @param child the child node or tree to add
     * */
    public void addChild(ConsistentStateTree child) {
        mChildNodes.add(child);
        child.mParentNode = this;
    }

    /**
     * Removes the given ConsistentStateTree node or subtree if it exists in this
     * ConsistentStateTree.
     *
     * @param treeToRemove the tree to remove
     * */
    public void removeStateTree(ConsistentStateTree treeToRemove) {
        Queue<ConsistentStateTree> queue = new LinkedList<>(mChildNodes);
        Set<ConsistentStateTree> visited = new ArraySet<ConsistentStateTree>();

        while(!queue.isEmpty()) {
            ConsistentStateTree next = queue.remove();

            if(!visited.contains(next)) {
                visited.add(next);
                queue.addAll(next.mChildNodes);

                if(next.equals(treeToRemove)) {
                    next.mParentNode.mChildNodes.remove(treeToRemove);
                    return;
                }
            }
        }
    }

    /**
     * Notify the neighbors of this ConsistentStateTree that a state change has occurred,
     * and to update their values as described in the documentation.
     *
     * @param updatedState the Object value representing the state that had changed
     * */
    public void propagateStateChange(Object updatedState) {
        pushStateUp(updatedState);
        pushStateDown(updatedState);
    }

    private void pushStateUp(Object updatedState) {
        if(mParentNode != null) {
            boolean childStatesAreConsistent = true;
            List<ConsistentStateTree> parentChildNodes = mParentNode.mChildNodes;

            for(int i = 0; i < parentChildNodes.size() - 1; i++) {
                Object currentNodeState = parentChildNodes.get(i).mState.value(),
                       nextNodeState = parentChildNodes.get(i + 1).mState.value();

                childStatesAreConsistent &= currentNodeState.equals(nextNodeState);
            }

            if(childStatesAreConsistent) {
                mParentNode.mState.update(updatedState);
                mParentNode.pushStateUp(updatedState);

                for(ConsistentStateTree child : mParentNode.mChildNodes) {
                    if(!child.mState.value().equals(updatedState)) {
                        child.pushStateDown(updatedState);
                    }
                }
            }
        }
    }

    private void pushStateDown(Object updatedState) {
        if(mChildNodes != null && mChildNodes.size() > 0) {
            for(ConsistentStateTree child : mChildNodes) {
                child.mState.update(updatedState);
                child.pushStateDown(updatedState);
            }
        }
    }

    /**
     * Represents the state value of a ConsistentStateTree node.
     *
     * When State is implemented in a class, the class implementing State must have an instance
     * variable that references the ConsistentStateTree that contains it. This is so state
     * changes can be propagated throughout the tree whenever the state of this object
     * changes.
     *
     * Additionally, each implementation of State must also override the equals method so
     * that state consistency can be checked, as well as for subtree removal.
     *
     * See ConfigureSwitch.java for an example implementation.
     * */
    public interface State {
        /**
         * Get the value of this state object.
         *
         * @return the state value
         * */
        public Object value();

        /**
         * Update this state object with the given value
         *
         * @param updatedValue the new value this state object should assume
         * */
        public void update(Object updatedValue);

        /**
         * Links this state object back to its containing ConsistentStateTree instance. This is
         * necessary to make propagateState work.
         *
         * @param tree the ConsistentStateTree object that contains this State value.
         * */
        public void setReferenceToContainingTree(ConsistentStateTree tree);
    }
}