package org.ihtsdo.otf.snomedboot.factory.implementation.standard;

import org.ihtsdo.otf.snomedboot.domain.Concept;
import org.ihtsdo.otf.snomedboot.domain.Description;
import org.ihtsdo.otf.snomedboot.domain.Relationship;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

public class ConceptImpl implements Concept {

	private final Long id;
	private String effectiveTime;
	private boolean active;
	private String moduleId;
	private String definitionStatusId;
	private String fsn;
	private final MultiValueMap<String, String> attributes;
	private final Set<Concept> inferredParents;
	private final Set<Long> memberOfRefsetIds;
	private final List<Relationship> relationships;
	private final List<Description> descriptions;

	public ConceptImpl(String id) {
		this.id = Long.parseLong(id);
		attributes = new LinkedMultiValueMap<>();
		inferredParents = new HashSet<>();
		memberOfRefsetIds = new HashSet<>();
		relationships = new ArrayList<>();
		descriptions = new ArrayList<>();
	}

	public ConceptImpl(String conceptId, String effectiveTime, boolean active, String moduleId, String definitionStatusId) {
		this(conceptId);
		this.effectiveTime = effectiveTime;
		this.active = active;
		this.moduleId = moduleId;
		this.definitionStatusId = definitionStatusId;
	}

	public void addMemberOfRefsetId(Long refsetId) {
		memberOfRefsetIds.add(refsetId);
	}

	@Override
	public Set<Long> getMemberOfRefsetIds() {
		return memberOfRefsetIds;
	}

	/**
	 * @return A set of all inferred ancestors
	 * @throws IllegalStateException if an active relationship is found pointing to an inactive parent concept
	 * or if an ancestor loop is found.
	 */
	@Override
	public Set<Long> getAncestorIds() throws IllegalStateException {
		final Stack<Long> stack = new Stack<>();
		stack.push(id);
		return collectInferredParentIds(this, new HashSet<Long>(), stack);
	}

	private Set<Long> collectInferredParentIds(ConceptImpl concept, Set<Long> ancestors, Stack<Long> stack) {
		for (Concept parentInt : concept.inferredParents) {
			ConceptImpl parent = (ConceptImpl) parentInt;
			if (!parent.isActive()) {
				throw new IllegalStateException("Is-a relationship points to inactive parent concept: " + concept.getId() + " -> " + parent.getId());
			}
			final Long parentId = parent.id;
			if (stack.contains(parentId)) {
				stack.push(parentId);
				throw new IllegalStateException("Ancestor loop detected: " + stack.toString());
			}
			ancestors.add(parentId);
			stack.push(parentId);
			collectInferredParentIds(parent, ancestors, stack);
			stack.pop();
		}
		return ancestors;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	public void addInferredParent(Concept parentConcept) {
		inferredParents.add(parentConcept);
	}

	public void removeInferredParent(Concept parentConcept) {
		inferredParents.remove(parentConcept);
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getEffectiveTime() {
		return effectiveTime;
	}

	@Override
	public String getModuleId() {
		return moduleId;
	}

	@Override
	public String getDefinitionStatusId() {
		return definitionStatusId;
	}

	public void setFsn(String fsn) {
		this.fsn = fsn;
	}

	@Override
	public String getFsn() {
		return fsn;
	}

	@Override
	public MultiValueMap<String, String> getAttributes() {
		return attributes;
	}

	public void addAttribute(String type, String value) {
		attributes.add(type, value);
	}

	public void addRelationship(Relationship relationship) {
		relationships.add(relationship);
	}

	@Override
	public List<Relationship> getRelationships() {
		return relationships;
	}

	public void addDescription(Description description) {
		descriptions.add(description);
	}

	@Override
	public List<Description> getDescriptions() {
		return descriptions;
	}

	@Override
	public String toString() {
		return id + " | " + fsn + " | ";
	}
}
