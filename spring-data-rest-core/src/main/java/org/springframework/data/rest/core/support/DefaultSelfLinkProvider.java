/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.rest.core.support;

import java.util.List;

import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.util.Assert;

/**
 * Default implementation of SelfLinkProvider that uses an {@link EntityLinks} instance to create self links. Considers
 * the configured {@link EntityLookup}s to use the returned resource identifier to eventually create the link.
 * 
 * @author Oliver Gierke
 * @since 2.5
 * @soundtrack Trio Rotation - Travis
 */
public class DefaultSelfLinkProvider implements SelfLinkProvider {

	private final PersistentEntities entities;
	private final EntityLinks entityLinks;
	private final PluginRegistry<EntityLookup<?>, Class<?>> lookups;

	/**
	 * Creates a new {@link DefaultSelfLinkProvider} from the {@link PersistentEntities}, {@link EntityLinks} and
	 * {@link EntityLookup}s.
	 * 
	 * @param entities must not be {@literal null}.
	 * @param entityLinks must not be {@literal null}.
	 * @param lookups must not be {@literal null}.
	 */
	public DefaultSelfLinkProvider(PersistentEntities entities, EntityLinks entityLinks,
			List<? extends EntityLookup<?>> lookups) {

		Assert.notNull(entities, "PersistentEntities must not be null!");
		Assert.notNull(entityLinks, "EntityLinks must not be null!");
		Assert.notNull(lookups, "EntityLookups must not be null!");

		this.entities = entities;
		this.entityLinks = entityLinks;
		this.lookups = OrderAwarePluginRegistry.create(lookups);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.rest.core.support.SelfLinkProvider#createSelfLinkFor(java.lang.Object)
	 */
	public Link createSelfLinkFor(Object instance) {

		Assert.notNull(instance, "Domain object must not be null!");

		return entityLinks.linkToSingleResource(instance.getClass(), getResourceId(instance));
	}

	/**
	 * Returns the identifier to be used to create the self link URI.
	 * 
	 * @param instance must not be {@literal null}.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Object getResourceId(Object instance) {

		Class<? extends Object> instanceType = instance.getClass();

		EntityLookup<Object> lookup = (EntityLookup<Object>) lookups.getPluginFor(instanceType);

		if (lookup != null) {
			return lookup.getResourceIdentifier(instance);
		}

		PersistentEntity<?, ?> entity = entities.getPersistentEntity(instanceType);

		if (entity == null) {
			throw new IllegalArgumentException(
					String.format("Cannot create self link for %s! No persistent entity found!", instanceType));
		}

		return entity.getIdentifierAccessor(instance).getIdentifier();
	}
}