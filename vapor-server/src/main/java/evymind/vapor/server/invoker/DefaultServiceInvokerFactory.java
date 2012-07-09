package evymind.vapor.server.invoker;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import evyframework.di.Binder;
import evyframework.di.BindingBuilder;
import evyframework.di.ConfigurationException;
import evyframework.di.DIBootstrap;
import evyframework.di.Inject;
import evyframework.di.Injector;
import evyframework.di.Key;
import evyframework.di.Module;
import evyframework.di.Provider;
import evyframework.di.spi.PerthreadScope;
import evymind.vapor.core.reflect.ClassInvoker;
import evymind.vapor.core.reflect.ClassInvokerFactory;
import evymind.vapor.core.reflect.DefaultClassInvokerFactory;
import evymind.vapor.core.reflect.ParameterResolverFactory;
import evymind.vapor.core.utils.component.AbstractLifecycle;

public class DefaultServiceInvokerFactory extends AbstractLifecycle implements ServiceInvokerFactory {

	private static final Logger log = LoggerFactory.getLogger(DefaultServiceInvokerFactory.class);

	private static final ParameterResolverFactory SERVICE_PARAMETE_RESOLVER_FACTORY = new ServiceParameterResolverFactory();

	private static final ClassInvokerFactory DEFA_CLASS_INVOKER_FACTORY = new DefaultClassInvokerFactory();

	private ClassInvokerFactory classInvokerFactory;

	private Injector injector;
	private List<ServiceDefinition> definitions = Lists.newCopyOnWriteArrayList();

	public DefaultServiceInvokerFactory() {

	}

	public DefaultServiceInvokerFactory(ClassInvokerFactory classInvokerFactory) {
		setClassInvokerFactory(classInvokerFactory);
	}

	public ClassInvokerFactory getClassInvokerFactory() {
		return classInvokerFactory == null ? DEFA_CLASS_INVOKER_FACTORY : classInvokerFactory;
	}

	public void setClassInvokerFactory(ClassInvokerFactory classInvokerFactory) {
		this.classInvokerFactory = classInvokerFactory;
	}

	@Override
	public synchronized ServiceInvoker getServiceInvoker(Class<?> serviceInterface) {
		return getServiceInvoker(serviceInterface.getName());
	}

	@Override
	public synchronized ServiceInvoker getServiceInvoker(String serviceName) {
		try {
			return injector.getInstance(Key.get(ServiceInvoker.class, serviceName));
		} catch (Exception e) {
			// no-op
		}
		return null;
	}

	@Override
	protected void doStart() throws Exception {
		injector = DIBootstrap.createInjector(new ServiceModule(definitions));
	}

	@Override
	protected void doStop() throws Exception {
		if (injector != null) {
			injector.shutdown();
		}
		injector = null;
	}

	public List<ServiceDefinition> getServiceDefinitions() {
		return definitions;
	}

	@Override
	public <T> void addService(Class<T> serviceInterface, T serviceInstance) {
		checkNotStarted();
		definitions.add(new ServiceDefinition(serviceInterface, serviceInstance));
	}

	@Override
	public <T> void addService(Class<T> serviceInterface, Class<? extends T> serviceImplementation) {
		checkNotStarted();
		definitions.add(new ServiceDefinition(serviceInterface, serviceImplementation, ServiceScope.singleton));
	}

	@Override
	public <T> void addService(Class<T> serviceInterface, Class<? extends T> serviceImplementation, ServiceScope scope) {
		checkNotStarted();
		definitions.add(new ServiceDefinition(serviceInterface, serviceImplementation, scope));
	}
	
	public void setServices(ServiceDefinition[] serviceDefinitions) {
		checkNotStarted();
		definitions.clear();
		if (serviceDefinitions != null) {
			for (ServiceDefinition serviceDefinition : serviceDefinitions) {
				definitions.add(serviceDefinition);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	class ServiceModule implements Module {

		private final List<ServiceDefinition> definitions;

		private ServiceModule(List definitions) {
			this.definitions = definitions;
		}

		@Override
		public void configure(Binder binder) {
			for (ServiceDefinition definition : definitions) {

				// binding services
				if (definition.getServiceInstance() != null) {
					binder.bind(definition.getServiceInterface()).toInstance(definition.getServiceInstance());
				} else {
					configureScope(
							binder.bind(definition.getServiceInterface()).to(definition.getServiceImplementation()),
							definition.getScope());
				}

				// binding service invoker
				Key<ServiceInvoker> key = Key.get(ServiceInvoker.class, definition.getServiceInterface().getName());
				configureScope(
						binder.bind(key).toProviderInstance(
								new ServiceInvokerProvider(definition.getServiceInterface())), definition.getScope());
			}
		}

		protected void configureScope(BindingBuilder builder, ServiceScope scope) {
			switch (scope) {
				case perthread:
					builder.in(PerthreadScope.INSTANCE);
					break;
				case none:
					builder.withoutScope();
					break;
				default:
					builder.inSingletonScope();
					break;
			}
		}

	}

	class ServiceInvokerProvider implements Provider<ServiceInvoker> {

		@Inject
		private Injector injector;

		private final Class<?> serviceInterface;

		private ServiceInvokerProvider(Class<?> serviceInterface) {
			this.serviceInterface = serviceInterface;
		}

		@Override
		public ServiceInvoker get() throws ConfigurationException {
			Object serviceInstance = injector.getInstance(serviceInterface);
			log.debug("Creating ServiceInvoker for {}", serviceInstance.getClass().getName());
			SpecificationsMethodsFilter methodsFilter = new SpecificationsMethodsFilter(serviceInterface);
			ClassInvoker classInvoker = getClassInvokerFactory().cteateClassInvoker(serviceInstance.getClass(),
					methodsFilter, SERVICE_PARAMETE_RESOLVER_FACTORY);
			return new DefaultServiceInvoker(classInvoker, serviceInstance);
		}
	}
}
