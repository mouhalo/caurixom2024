package com.caurix.distributorauto.expandablerecyclerview.listeners;

public interface OnGroupClickListener {

  /**
   * @param flatPos the flat position (raw index within the list of visible items in the
   * RecyclerView of a GroupViewHolder)
   * @return false if click expanded group, true if click collapsed group
   */
  boolean onGroupClick(int flatPos);
}