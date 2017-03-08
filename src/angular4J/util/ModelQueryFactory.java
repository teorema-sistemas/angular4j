package angular4J.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Named;

import angular4J.context.NGSessionScoped;
import angular4J.util.ModelQuery;

@NGSessionScoped
@Named("ModelQueryFactory")
public class ModelQueryFactory implements Serializable {

   private static final long serialVersionUID = 1L;

   private final Map<Class, ModelQuery> allQueries = new HashMap<>();

   RootScope rootScope = new RootScopeImpl();

   public ModelQuery get(Class clazz) {
      if (allQueries.get(clazz) == null) addQuery(clazz);
      ModelQueryImpl query = (ModelQueryImpl) allQueries.get(clazz);
      query.setOwner(clazz);
      return query;

   }

   @Produces
   public ModelQuery getModelQuery(InjectionPoint injectionPoint) {

      ModelQuery query = get(injectionPoint.getMember().getDeclaringClass());

      return query;
   }

   public void addQuery(Class clazz) {
      allQueries.put(clazz, new ModelQueryImpl());
   }

   @Produces
   public RootScopeImpl getRootScope() {
      return (RootScopeImpl) rootScope;
   }
}
