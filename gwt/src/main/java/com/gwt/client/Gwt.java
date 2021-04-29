package com.gwt.client;

import java.util.ArrayDeque;
import java.util.ArrayList;
import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

//This class is used as an entry point for the program and for the animation performing.
public class Gwt extends Animation implements EntryPoint {
	private int i, j, low, high, row, // low an high bounds of the sorted subarray, current row number
			middle, bearing, temp; // pivot element for the sorting algorithm
	private static int values[], amount, lastSize, lastLow, lastHigh,
			// the array of sorted random numbers, its size and subarray bounds,
			delay = 500, duration = 10000000; // animation delay and its duration
	static final String HOWMANY = "How many numbers to display?", BETWEEN = "Please enter a number between 0 and 100.",
			SMALLER = "Please select a value smaller or equal to 30.", GREEN = "greenButton whiteText",
			BLUE = "blueButton whiteText", LG = "lightgreenButton whiteText"; // color style constants and info messages
	private boolean isForReduce, isNormalStart = true, isCompleted, isI; // controlling variables
	private char phase = 'a', p = 'a'; // phase of the animation performing
	private static ArrayDeque<Gwt> deque; // this dequeue is used as a stack for the recursion
	private static ArrayList<Button> buttons; // list of presented buttons
	private static Gwt current; // object used as a current animation thread
	static {
		deque = new ArrayDeque<Gwt>();
	}
	private static long time1 = 0L; // it stores the current time
	private static boolean isInit = true, isAscending; // shows if the state is initial and the sorting order
	private static final Button sortButton = new Button("Sort"); // the button for sorting animation
	private static final Label bound = new Label(""); // pops up when the button with number bigger than 30 is selected

	Gwt() // The constructor to create the main initial Gwt object.
	{
		i = j = low = high = 0;
	}

	Gwt(int l, int h) // The constructor to create new object during the sorting process.
	{
		i = low = l; // when the animation is in process and the main array,
		j = high = h; // divides into two subarrays to process each of them
	}

	public void onModuleLoad() { // starting point of the program file
		final Button enterButton = new Button("Enter"), resetButton = new Button("Reset"); // option buttons
		final VerticalPanel enterPanel = new VerticalPanel(), optionsPanel = new VerticalPanel();
		final HorizontalPanel sortPanel = new HorizontalPanel(); // graphical panels
		final Grid grid = new Grid(10, 10); // grid to place the numbered buttons
		final TextBox countField = new TextBox(); // field to input the buttons amount
		final Label howmany = new Label(HOWMANY), between = new Label(""); // info labels
		setParameters(howmany, enterPanel, 200, 20, "EnterLabel", true, null);
		setParameters(between, enterPanel, 250, 20, "EnterLabel", true, null);
		setParameters(countField, enterPanel, 80, 20, "TextBox", true, null);
		setParameters(grid, sortPanel, -1, -1, "grid", true, null);
		setParameters(optionsPanel, sortPanel, -1, -1, "option", true, null);
		setParameters(enterButton, enterPanel, 80, 30, BLUE + " EnterButton", true, (ClickEvent e) -> {
			// The button to input the count of the buttons.
			String input = countField.getText(); // the number entered by the user
			if (input != null) // validation of the input string
				if (input.matches("\\d{1,3}")) {
					int number = Integer.parseInt(input);
					if (number <= 100) // in case of correct input
					{ // fills the array with random numbers and locates them in the grid,
						resetArray(number, grid); // resets all the buttons values
						between.setText(""); // warning label is not needed
						sortButton.setEnabled(number > 1);
						isAscending = false; // the sorting order is descending by default
						enterPanel.setVisible(false); // switches from main screen
						sortPanel.setVisible(true); // to sort screen
						return;
					}
				}
			between.setText(BETWEEN); // the warning message for the incorrect input
		});
		// The button which sorts the numbered buttons.
		setParameters(sortButton, optionsPanel, 80, 30, LG, true, (ClickEvent e) -> {
			bound.setText(""); // warning window is not needed
			cancel(true, true); // canceling the previous sorting
			if (values.length < 2) // no need to sort for this amount
				return;
			for (Button b : buttons) // paints all buttons green on start
			{
				b.setStyleName(GREEN); // using css color style
				b.setEnabled(false); // and disables these buttons
			}
			lastLow = 0; // variables for repainting buttons
			lastHigh = amount - 1; // on each animation step
			i = low = 0; // setting the low and high
			j = high = amount - 1; // positions of sorting
			current = this; // this animation thread is current
			run(duration); // starting the animation
		});
		// The button to reset array with new amount of buttons.
		setParameters(resetButton, optionsPanel, 80, 30, LG, true, (ClickEvent e) -> {
			cancel(true, true); // stops the previous sorting
			bound.setText(""); // warning window is not needed
			sortPanel.setVisible(false); // switches from sort screen
			enterPanel.setVisible(true); // to main screen
		});
		grid.setCellPadding(5); // sets the spaces in the grid
		optionsPanel.setSpacing(15);
		setParameters(bound, optionsPanel, 100, 100, "block", true, null);
		setParameters(enterPanel, "startpage", "Screen paddingTop", true); // enter screen
		setParameters(sortPanel, "sortpage", "Screen", false); // sorting screen
	} 

	// The method to attach the widget and set its parameters.
	static void setParameters(Widget item, Panel place, int w, int h, String style, boolean vis, ClickHandler lstnr) {
		if (w != -1)
			item.setPixelSize(w, h); // sets the size of the element
		if (lstnr != null && item instanceof FocusWidget) // sets the handler
			((FocusWidget) item).addClickHandler(lstnr);// for the button
		setParameters(item, "", style, vis); // calling the overloaded method
		if (place != null) // attaching the widget to its place
			place.add(item);
	}

	static void setParameters(Widget item, String id, String style, boolean vis) {
		if (!style.isEmpty()) // sets the visual style using the css file
			item.setStyleName(style);
		if (!id.isEmpty()) // attaches the widget to the html page
			RootPanel.get(id).add(item); // using the id as a place for it
		item.setVisible(vis); // displays it on the screen
	}

	// The method to reset array, renew all the numbered buttons with new amount.
	void resetArray(int count, Grid w) {
		if (!isInit && isRunning()) { // stops the previous sorting and repaints all the buttons to blue
			cancel(true, false);
		}
		lastSize = amount; // remembers the last size
		amount = count; // new size of the array
		if (isInit || count != lastSize) // when to add or remove buttons
			values = new int[amount]; // changes the size of the array
		for (int i = 0; i < amount; i++) {
			values[i] = (int) (Math.random() * 1000) + 1;
		} // fills it with the random numbers between 1 and 1000
		int some = (int) (Math.random() * amount);
		if (values[some] > 30) // one of the numbers must be equal to 30 or less
			values[some] = (int) (Math.random() * 30) + 1;
		if (isInit) // if the buttons set is forming for the first time
			buttons = new ArrayList<Button>(amount); // creating the list of buttons
		if (isInit || count != lastSize) // adds or removes some buttons if needed
			changeButtonsCount(amount, w);
		for (int i = 0; i < amount; i++) // displays the numbers on the buttons
			buttons.get(i).setText((values[i]) + "");
		isInit = false; // makes some actions occur once a program call
	}

	// The method to change the number of the buttons after the reset.
	void changeButtonsCount(int finalCount, Grid w) {
		if (finalCount < lastSize) // if it has to be less than previous one
			for (int i = lastSize - 1; i >= finalCount; i--)
				buttons.get(i).setVisible(false); // hides the excess buttons
		else
			for (int i = lastSize; i < finalCount; i++) // in other case
			{
				if (i >= buttons.size()) // the new button will be created
				{
					if (row == 10) // if there are 10 buttons in this column
						row = 0; // moves to the next one
					Button bttn = new Button(values[i] + "");
					buttons.add(bttn); // adds it to the list of numbered buttons
					w.setWidget(row++, i / 10, bttn); // attaches it to the grid
					// sets the parameters of the button such as size, color style and handler
					setParameters(bttn, null, 65, 25, BLUE, true, (ClickEvent e) -> {
						int selected = Integer.valueOf(((Button) (e.getSource())).getText());
						if (selected > 30) // if the number bigger than 30 is chosen
							bound.setText(SMALLER); // offers to select the less number
						else { // in other case - resets the values with the new numbers
							resetArray(selected, w); // in the amount of the selected button number
							sortButton.setEnabled(selected > 1);
							bound.setText("");
						}
					});
				} else
					buttons.get(i).setVisible(true); // displays the button if it already exists
			}
	}

	@Override
	protected void onStart() // triggers when the animation is started
	{
		if (isNormalStart) // if the duration time is not elapsed prematurely
			phase = 'a'; // initial phase of the animation
		isCompleted = false; // animation is in progress
		time1 = System.currentTimeMillis(); // stores the current time
	}
	/*
	 * This realization of sorting uses the different phases of animation actions,
	 * each of them comes when the time delay is elapsed.
	 */

	void startActions() { // This method calls when the new loop of sorting recursion is starting
		if (isNormalStart) // paints the involved buttons green, others - blue
		{
			int sG = low, fG = high + 1, sB = lastLow, fB = lastHigh + 1, sP = 0, fP = 0;
			boolean within = false, greenMid = false;
			if (!(low == lastLow && high == lastHigh)) {
				within = (!(low < lastLow ^ high > lastHigh));
				if (within) // if the current subarray is within the last one
				{
					greenMid = sG > sB || fB > fG;
					sP = greenMid ? sG : sB;
					fP = greenMid ? fB : fG;
				} else if (high > lastHigh) {
					sG = lastHigh + 1;
					fB = low;
				} else {
					fG = lastLow;
					sB = high + 1;
				}

				if (within) // repaints the buttons using css styles
				{
					for (int i = (greenMid ? sB : sG); i < fP; i++) {
						if (i == sP) {
							i = greenMid ? fG : fB;
							if (i >= values.length)
								break;
						}
						buttons.get(i).setStyleName(greenMid ? BLUE : GREEN);
					}
				}

				else {
					for (int i = sG; i < fG; i++)
						buttons.get(i).setStyleName(GREEN);
					for (int i = sB; i < fB; i++)
						buttons.get(i).setStyleName(BLUE);
				}
			}
			lastLow = low; // remembers the current subarray bounds
			lastHigh = high; // to repaint next time
			if (low >= high || values.length == 0) // if its length equals 0,
			{ // no need to divide this subarray
				isForReduce = true; // marks that the subarray is not to be divided
				cancel();// this subarray will not go through the sorting
				return;
			}
			middle = low + (high - low) / 2; // number of the pivot element in the middle
			bearing = values[middle]; // and value of it
			buttons.get(middle).setStyleName("purpleButton whiteText"); // changes color to purple
			i = low; // iteration starts from its current bound positions
			j = high;
		} else
			isNormalStart = true;
	} // exits from the state of premature termination

	@Override
	protected void onUpdate(double progress) { // triggers on each animation step
		if (System.currentTimeMillis() - time1 >= delay) // if delay time is elapsed
		{
			if (phase == 'a') // initial phase
			{
				startActions(); // actions on start
				p = 'a';
			} else {
				p = 'n'; // new thread, ready to run
				if (i <= j) // if the lower chosen element is not upper than higher
					switch (phase) // the phase determines the further actions
					{
					case 'n': // if the animation is just started, new
					case 'i': // or in the phase of incrementing of i
						/*
						 * isAscending determines the sorting order, if current element is less or
						 * bigger than the pivot depending on this, highlights it with the border style.
						 */
						if (isAscending ? (values[i] < bearing) : (values[i] > bearing)) {
							buttons.get(i).addStyleName("cherryBorder");
							p = 'I'; // moving to the next increment phase of i
							break; // exit the switch operator
						}
					case 'j': // in the phase of decrementing of j
						if (isAscending ? (values[j] > bearing) : (values[j] < bearing)) {
							buttons.get(j).addStyleName("cherryBorder");
							p = 'J'; // moving to the next incrementing phase of j
							break;
						} // if current element is less or bigger than the pivot depending on isAscending
						highlight(i, j, true); // on this step search of i and j is finished
						p = 's'; // highlighting the button text, moving to swap phase
						break; // breaking the animation step invokes the delay to display
					case 'I': // incrementing phase of i, removing the border after the delay
					{
						buttons.get(i).removeStyleName("cherryBorder");
						i++; // choosing the next lower element to compare with the pivot
						p = 'i'; // coming back to the increment loop of i
						break;
					}
					case 'J': // decrementing phase of j, removing the border after the delay
					{
						buttons.get(j).removeStyleName("cherryBorder");
						j--; // choosing the next upper element to compare with the pivot
						p = 'j'; // coming back to the decrement loop of j
						break;
					} // breaking the animation step invokes the delay to display
					case 'M': // the phase to relocate the middle element
					{ // and undo the highlight after displaying the swap
						highlight(middle, middle + (isI ? 1 : -1), false);
						if (isI) // this variable shows if i or j equals middle
							i++;
						else
							j--; // the chosen element is put on the position of pivot
						middle = isI ? i : j; // and the pivot changes its position with the offset
						p = 'm';// coming back to the loop of swapping with the middle
						break;
					}
					case 's':// if the appropriate element is found and it needs to swap
						if (!((isI = i == middle) || (j == middle))) // if it is not middle
						{
							if (i != j) // if the elements are different, they swap
							{
								temp = values[i];
								values[i] = values[j];
								values[j] = temp;
								buttons.get(i).setText(values[i] + ""); // and their values change
								buttons.get(j).setText(values[j] + "");
							}
							p = 'x'; // moving to the exit phase
							break;
						} // displaying the swapping using the delay
					case 'm': // when i or j equals middle, the pivot is chosen
					{
						if (i < j) // while elements are not the same
						{
							if (isAscending ? values[j] < values[i] : values[j] > values[i])
							// if elements are compared and need to swap
							{
								temp = values[isI ? j : i]; // remembering the non-middle value
								for (int t = isI ? j : i; isI ? t > i : t < j; t += isI ? -1 : 1) {
									values[t] = values[isI ? t - 1 : t + 1];
									buttons.get(t).setText(values[t] + "");
								} // offsets the other elements to the place of chosen in its direction
								values[middle] = temp;// chosen element takes the place of the middle one
								buttons.get(middle).setText(temp + "");// changes its text and style
								buttons.get(middle).setStyleName("greenButton cherryText cherryBorder");
								buttons.get(middle + (isI ? 1 : -1))
										.setStyleName("purpleButton cherryText cherryBorder");
								p = 'M';
							} // moving to phase M to relocate the middle element
							else // if the element less (or bigger) than pivot is not found
							{
								if (isI) // chose the next element after the chosen
									j--; // which is not middle
								else
									i++;
								p = 'm';
							} // coming back to the loop of swapping with the middle
							break;
						}
						p = 'y'; // moving to the exit phase when i equals j
						isForReduce = high - low < 2; // in this case this subarray will not be divided
						break;
					}
					case 'x': // the exit phase after the swapping non-middle elements
						p = 'i'; // moving to the increment phase to find new elements
						highlight(i, j, false); // undo the highlight
						i++; // the search starts from the next element upper the current
						j--; // and from the next one lower than the current higher one too
						break; // displaying the result of changes
					case 'y': // the exit phase after the final swapping with the pivot
						highlight(middle, middle, false); // undo the highlight
						p = 'z';
					} // finish when the swapping with the pivot is impossible
			}

			if (p == 'n' || p == 'z') // if this step did not invoke the actions
			{ // or it is the final phase after the final pivot swap
				cancel(); // cancels the animation of this subarray
				return;
			}
			time1 = System.currentTimeMillis(); // remembers the current time
			if (p == 'a') // after the start actions performing
				phase = 'n'; // the thread will be new and ready for usage
			else
				phase = p;
		}
	} // the thread will move to the chosen phase

	void highlight(int i, int j, boolean on) // This method highlights the elements
	{ // which are swapping making the border and coloring the text
		buttons.get(i).removeStyleName(on ? "whiteText" : "cherryText");
		buttons.get(j).removeStyleName(on ? "whiteText" : "cherryText");
		buttons.get(i).addStyleName(on ? "cherryText" : "whiteText");
		buttons.get(j).addStyleName(on ? "cherryText" : "whiteText");
		if (on) // makes the border when the elements are swapped
		{
			buttons.get(i).addStyleName("cherryBorder");
			buttons.get(j).addStyleName("cherryBorder");
		} else { // and removes it after the delay for displaying
			buttons.get(i).removeStyleName("cherryBorder");
			buttons.get(j).removeStyleName("cherryBorder");
		}
	}

	@Override
	protected void onComplete() { // if the duration time elapsed
		if (isCompleted) // if the animation has been already completed,
			finish(true); // finishes it
		else {
			isNormalStart = false; // or if just the duration time elapsed
			run(duration);
		} // the animation continues performing
	}

	@Override
	protected void onCancel() { // triggers when the animation is stopped
		if (isCompleted) // if the animation is already completed
			return; // no need to do any actions
		buttons.get(middle).setStyleName(GREEN); // removes the pivot mark
		if (!isForReduce) // if the subarray is possible to divide
		{ // it would be divided into the one or two subarrays
			if ((low < j) || (high > i)) {
				if (low < j) // if the lower subarray under the pivot exists
				{
					if (high > i) // and also the upper subarray exists
						deque.push(new Gwt(i, high));// pushes into the stack the last one
					high = j; // sets the bound of the subarray
				} // the current object would represent the lower one
				else
					low = i;// or represent the upper one if only it exists
				run(duration);// run the animation as a higher or lower subarray
				return;
			}
		}
		if (!deque.isEmpty())// when this subarray can not be divided
			// but the subarrays stored in the stack and still not launched exist
			(current = deque.pop()).run(duration); // launches the last of them
		else
			finish(true);
	} // there is nothing to launch, finish the animation

	void cancel(boolean makesInterrupt, boolean makesRepaint) // cancels the animation
	{
		if (makesInterrupt) // when to stop the whole animation process
			finish(makesRepaint); // calls the finish method
		else
			cancel();
	} // else cancels only the current step

	private void finish(boolean makesPaintBlue)// stops the whole animation process
	{
		if (current != null) // the animation object which is running at the moment
		{
			current.isCompleted = true; // marks it as the completed one to
			current.cancel();
		} // cancel the current thread not invoking the next step
		current = null; // clears the current thread
		deque.clear(); // clears the calling stack
		isAscending = !isAscending; // changing the sort order to opposite
		for (Button b : buttons)
			b.setEnabled(true);
		if (makesPaintBlue) // as the sorting is ready or cancelled
			for (Button b : buttons) // paints all the buttons blue
				b.setStyleName(BLUE); // using the color css style
	}
}
