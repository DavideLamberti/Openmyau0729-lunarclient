package myau;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EventBus {
   private final Map<Class<? extends Event>, List<EventListener>> listeners = new LinkedHashMap<>();

   public void register(Object owner) {
      for (Method method : owner.getClass().getDeclaredMethods()) {
         EventHandler handler = method.getAnnotation(EventHandler.class);
         if (handler != null) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 1 && Event.class.isAssignableFrom(parameterTypes[0])) {
               Class<? extends Event> eventType = (Class<? extends Event>)parameterTypes[0];
               if (!method.isAccessible()) {
                  method.setAccessible(true);
               }

               List<EventListener> eventListeners = this.listeners.get(eventType);
               if (eventListeners == null) {
                  eventListeners = new ArrayList<>();
                  this.listeners.put(eventType, eventListeners);
               }

               eventListeners.add(new EventListener(owner, method, handler.priority()));
               Collections.sort(eventListeners, Comparator.comparingInt(EventListener::getPriority).reversed());
            }
         }
      }
   }

   public Event post(Event event) {
      List<EventListener> eventListeners = this.listeners.get(event.getClass());
      if (eventListeners == null) {
         return event;
      } else {
         for (EventListener listener : eventListeners) {
            try {
               listener.getMethod().invoke(listener.getOwner(), event);
            } catch (InvocationTargetException | IllegalArgumentException | IllegalAccessException var6) {
            }
         }

         return event;
      }
   }
}
