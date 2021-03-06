/**
 * Copyright 2011 The PlayN Authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package playn.core;

import java.util.List;
import java.util.ArrayList;

/**
 * Provides implementations for per-platform concrete {@link GroupLayer}s. Because of single
 * inheritance (and lack of traits) we have to delegate this implementation rather than provide an
 * abstract base class.
 */
public class GroupLayerImpl<L extends AbstractLayer>
{
  /** This group's children. */
  public List<L> children = new ArrayList<L>();

  /**
   * @return the index into the children array at which the layer was inserted (based on depth).
   */
  public int add(GroupLayer self, L child) {
    // check whether the last child has the same depth as this child, in which case append this
    // child to our list; this is a fast path for when all children have the same depth
    int count = children.size(), index;
    if (count == 0 || children.get(count-1).depth() == child.depth()) {
      index = count;
    } else {
      // otherwise find the appropriate insertion point via binary search
      index = findInsertion(child.depth());
    }

    // remove the child from any existing parent, preventing multiple parents
    if (child.parent() != null) {
      child.parent().remove(child);
    }
    children.add(index, child);
    child.setParent(self);
    child.onAdd();
    return index;
  }

  // TODO: remove this when GroupLayer.add(int,Layer) is removed
  public void add(GroupLayer self, int index, L child) {
    // remove the child from any existing parent, preventing multiple parents
    if (child.parent() != null) {
      child.parent().remove(child);
    }
    children.add(index, child);
    child.setParent(self);
    child.onAdd();
  }

  public void remove(GroupLayer self, L child) {
    int index = findChild(child, child.depth());
    if (index < 0) {
      throw new UnsupportedOperationException(
        "Could not remove Layer because it is not a child of the GroupLayer");
    }
    remove(index);
  }

  // TODO: remove this when GroupLayer.remove(int) is removed
  public void remove(GroupLayer self, int index) {
    remove(index);
  }

  public void clear(GroupLayer self) {
    while (!children.isEmpty()) {
      remove(children.size() - 1);
    }
  }

  public void destroy(GroupLayer self) {
    AbstractLayer[] toDestroy = children.toArray(new AbstractLayer[children.size()]);
    // first remove all children efficiently
    self.clear();
    // now that the children have been detached, destroy them
    for (AbstractLayer child : toDestroy) {
      child.destroy();
    }
  }

  public void onAdd(GroupLayer self) {
    for (L child : children) {
      child.onAdd();
    }
  }

  public void onRemove(GroupLayer self) {
    for (L child : children) {
      child.onRemove();
    }
  }

  /**
   * @return the new index of the depth-changed layer.
   */
  public int depthChanged(GroupLayer self, Layer layer, float oldDepth) {
    // structuring things such that Java's type system knew what was going on here would require
    // making AbstractLayer and ParentLayer more complex than is worth it
    @SuppressWarnings("unchecked") L child = (L)layer;

    // it would be great if we could move an element from one place in an ArrayList to another
    // (portably), but instead we have to remove and re-add
    int oldIndex = findChild(child, oldDepth);
    children.remove(oldIndex);
    int newIndex = findInsertion(child.depth());
    children.add(newIndex, child);
    return newIndex;
  }

  private void remove(int index) {
    L child = children.remove(index);
    child.onRemove();
    child.setParent(null);
  }

  // uses depth to improve upon a full linear search
  private int findChild(L child, float depth) {
    // findInsertion will find us some element with the same depth as the to-be-removed child
    int startIdx = findInsertion(depth);
    // search down for our child
    for (int ii = startIdx-1; ii >= 0; ii--) {
      L c = children.get(ii);
      if (c == child) {
        return ii;
      }
      if (c.depth() != depth) {
        break;
      }
    }
    // search up for our child
    for (int ii = startIdx, ll = children.size(); ii < ll; ii++) {
      L c = children.get(ii);
      if (c == child) {
        return ii;
      }
      if (c.depth() != depth) {
        break;
      }
    }
    return -1;
  }

  // who says you never have to write binary search?
  private int findInsertion(float depth) {
    int low = 0, high = children.size()-1;
    while (low <= high) {
      int mid = (low + high) >>> 1;
      float midDepth = children.get(mid).depth();
      if (depth > midDepth) {
        low = mid + 1;
      } else if (depth < midDepth) {
        high = mid - 1;
      } else {
        return mid;
      }
    }
    return low;
  }
}
