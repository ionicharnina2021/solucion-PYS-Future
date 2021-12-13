package control;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import model.customer.BiasCustomer;
import model.customer.CustomerCard;
import model.park.AdapterParkCustormerCard;
import model.park.Fraction;
import model.park.Park;
import modelAtractions.FairFactory;
import modelAtractions.FairGround;

public class WorkingDay implements Runnable {

	private Park park;
	private Long totalRides=0l, totalVisitors=0l,happyVisitors=0l;
	private final int maxConcurrentVisitors=100;
	private final Long maxRides=1000l;
	private float correntRate = 5;
	private ArrayList<Future<Fraction>> customers=new ArrayList<>();
	private int concurrentVisitors=0;

	public WorkingDay() throws Exception {
		super();
		park = new Park(FairFactory.Performance,FairFactory.Performance,FairFactory.Show,FairFactory.Show,FairFactory.RollerCoaster,FairFactory.RollerCoaster);
	}

	@Override
	public void run() {
		ExecutorService executorServiceCustomer = Executors.newCachedThreadPool();
		while (!endOfDay()) {
			for (int i = 0; i < calculateNewVisitors(); i++) {
				BiasCustomer biasCustomer = new BiasCustomer(20, this);
				customers.add(executorServiceCustomer.submit(biasCustomer));
				concurrentVisitors++;
				totalVisitors++;
			}
			Comment.talk("visitantes concurrentes "+concurrentVisitors+" y totales "+totalVisitors);
		}
		try {
			executorServiceCustomer.awaitTermination(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("happy users "+calculateHappyUsers());
		System.out.println("fairgrounds valoration "+park.getCurrentAverageRate());
		executorServiceCustomer.shutdown();
	}

	private double calculateHappyUsers() {
		
		return customers.stream().filter((frac)->{
			try {
				return frac.get().getCurrentValue()>BiasCustomer.minimumEnjoyment;
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			return false;
			}).count()*100/totalVisitors;
	}

	private int calculateNewVisitors() {
		return maxConcurrentVisitors-concurrentVisitors;
	}

	private boolean endOfDay() {
		return totalRides>=maxRides;
	}

	public CustomerCard anotateCard(CustomerCard takeRide) {
		totalRides++;
		new AdapterParkCustormerCard().insertFairgorundRate(park,takeRide);
		return takeRide;
	}

	public void endVisitation(BiasCustomer biasCustomer) {
		addHappyUser(biasCustomer);
		customers.remove(biasCustomer);
	}

	private void addHappyUser(BiasCustomer biasCustomer) {
		if(biasCustomer.getCurrentValue()>5) {
			happyVisitors++;
		}
		
	}

	public void finalizeVisitor() {
		concurrentVisitors--;
	}

	public FairGround getFairGround(int i) {
		return park.getFairGround(i);
	}

	public int getFairGroundsSize() {
		return park.getFairGroundsSize();
	}
}
