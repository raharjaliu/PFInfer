package filter;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class QueueBuilder {

	private Queue<String> queue;
	HashSet<String> branches;
	HashSet<String> leaves;

	public QueueBuilder(Set<String> cellNumbers) {
		this.queue = new LinkedList<String>();
		this.branches = new HashSet<String>();
		this.leaves = new HashSet<String>();
		searchTree("1", cellNumbers);
	}

	public HashSet<String> getBranches() {
		return (this.branches);
	}

	public HashSet<String> getLeaves() {
		return (this.leaves);
	}

	public Queue<String> getQueue() {
		return (this.queue);
	}

	public String getParent(String child) {
		int childNumber = Integer.parseInt(child);
		if ((childNumber % 2) == 0) {
			return (Integer.toString(childNumber / 2));
		} else {
			return (Integer.toString((childNumber - 1) / 2));
		}
	}

	private void searchTree(String current, Set<String> cellNumbers) {
		if (cellNumbers.contains(current)) {
			this.queue.add(current);
			int number = Integer.parseInt(current);
			String firstchild = Integer.toString(2 * number);
			String secondchild = Integer.toString((2 * number) + 1);

			if (cellNumbers.contains(firstchild)
					|| cellNumbers.contains(secondchild)) {
				searchTree(firstchild, cellNumbers);
				searchTree(secondchild, cellNumbers);
				this.branches.add(current);
			} else {
				this.leaves.add(current);
			}
		}
	}
}
