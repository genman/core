/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.tests.builtinBeans.metadata.passivation;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionPoint;

import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.injection.ForwardingInjectionPoint;

/**
 * Registers {@link Car} as a bean using {@link PassivationCapableBeanImpl}.
 *
 * @author Jozef Hartinger
 *
 */
public class CarExtension implements Extension {

    private PassivationCapableBeanImpl<Car> bean;

    public void registerCar(@Observes AfterBeanDiscovery event, BeanManager manager) {
        AnnotatedType<Car> annotatedType = manager.createAnnotatedType(Car.class);
        final BeanAttributes<Car> attributes = manager.createBeanAttributes(annotatedType);
        this.bean = new PassivationCapableBeanImpl<Car>(Car.class, attributes);
        BeanManagerProxy proxy = (BeanManagerProxy) manager;
        final InjectionTarget<Car> injectionTarget = proxy.delegate().createInjectionTarget(annotatedType, this.bean);
        this.bean.setInjectionTarget(injectionTarget);
        event.addBean(bean);
    }

    void wrapInjectionPoints(@Observes ProcessInjectionPoint<Car, ?> event) {
        final InjectionPoint delegate = event.getInjectionPoint();
        if (delegate.getBean() == null) {
            event.setInjectionPoint(new ForwardingInjectionPoint() {

                @Override
                public Bean<?> getBean() {
                    return bean;
                }

                @Override
                protected InjectionPoint delegate() {
                    return delegate;
                }
            });
        }
    }
}
