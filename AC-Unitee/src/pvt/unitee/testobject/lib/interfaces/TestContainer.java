package pvt.unitee.testobject.lib.interfaces;

import java.util.List;

import pvt.unitee.reporter.lib.IssueId;
import pvt.unitee.testobject.lib.java.JavaTestClassInstance;
import pvt.unitee.testobject.lib.loader.DataMethodsHandler;
import pvt.unitee.testobject.lib.loader.group.Group;

public interface TestContainer extends TestObject {

	void loadInstances() throws Exception;

	int getInstanceCount();

	Class<?> getUserTestClass();

	void addExecutableCreatorName(String name);

	List<JavaTestClassInstance> getInstances();

	int getInstanceThreadCount();

	DataMethodsHandler getDataMethodsHandler();

	void resetExecutorCreatorQueue();

	void load() throws Throwable;

	boolean areInstancesCreated();

	boolean shouldExecute(IssueId outId);

	void setGroup(Group g) throws Exception;

	boolean hasCompleted();

	void markTestClassInstanceCompleted(TestContainerInstance instance);

	void setAllScheduledCreators(List<String> scheduledCreatorsForContainer);
}