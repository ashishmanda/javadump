package cmsc433.p3;

import java.util.LinkedList;
import java.util.List;

import java.util.concurrent.ForkJoinPool;



/**
 * This file needs to hold your solver to be tested. You can alter the class to
 * extend any class that extends MazeSolver. It must have a constructor that
 * takes in a Maze. It must have a solve() method that returns the datatype
 * List<Direction> which will either be a reference to a list of steps to take
 * or will be null if the maze cannot be solved.
 */
public class StudentMTMazeSolver extends SkippingMazeSolver {
	public StudentMTMazeSolver(Maze maze) {
		super(maze);
	}

	private int processors = Runtime.getRuntime().availableProcessors();
	private ForkJoinPool fjp = new ForkJoinPool(processors);
	
	public List<Direction> solve() {
		MazeTask mt = new MazeTask(this.maze);
		LinkedList<Direction> returnval = fjp.invoke(mt);
		
		return returnval;
	}
}
