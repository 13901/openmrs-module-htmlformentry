package org.openmrs.module.htmlformentry.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlformentry.FormEntryContext;
import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
import org.openmrs.module.htmlformentry.FormEntryException;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.FormSubmissionError;
import org.openmrs.module.htmlformentry.HtmlFormEntryUtil;
import org.openmrs.module.htmlformentry.action.FormSubmissionControllerAction;
import org.openmrs.module.htmlformentry.widget.DateWidget;
import org.openmrs.module.htmlformentry.widget.ErrorWidget;
import org.openmrs.util.OpenmrsUtil;

/**
 * Serves as both the HtmlGeneratorElement and the FormSubmissionControllerAction for a Program
 * Enrollment.
 */
public class EnrollInProgramElement implements HtmlGeneratorElement, FormSubmissionControllerAction {
	
	private Program program;
	
	private List<ProgramWorkflowState> states;
	
	private DateWidget dateWidget;
	
	private ErrorWidget dateErrorWidget;
	
	public EnrollInProgramElement(FormEntryContext context, Map<String, String> parameters) {
		try {
			program = HtmlFormEntryUtil.getProgram(parameters.get("programId"));
			if (program == null)
				throw new FormEntryException("");
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("Couldn't find program in: " + parameters);
		}
		
		List<PatientProgram> patientPrograms = Context.getProgramWorkflowService().getPatientPrograms(
		    context.getExistingPatient(), program, null, null, null, null, false);
		PatientProgram pp = null;
		if (patientPrograms.size() == 1)
			pp = patientPrograms.get(0);
		
		if ("true".equalsIgnoreCase(parameters.get("showDate"))) {
			//don't show the enroll date widget for completed programs
			if (pp == null || pp.getDateCompleted() == null) {
				dateWidget = new DateWidget();
				dateErrorWidget = new ErrorWidget();
				context.registerWidget(dateWidget);
				context.registerErrorWidget(dateWidget, dateErrorWidget);
			}
		}
		
		String stateIdsStr = parameters.get("stateIds");
		if (StringUtils.isNotBlank(stateIdsStr)) {
			states = new ArrayList<ProgramWorkflowState>();
			String[] stateIdsUuidsOrPrefNames = stateIdsStr.split(",");
			//set to store unique work flow state combinations so as to determine multiple states in same work flow
			Set<String> workflowsAndStates = new HashSet<String>();
			for (String value : stateIdsUuidsOrPrefNames) {
				value = value.trim();
				ProgramWorkflowState state = null;
				if (value.indexOf(":") > -1) {
					//search state by its underlying concept that matched the mapping
					String[] sourceAndCode = value.split(":");
					Concept concept = Context.getConceptService().getConceptByMapping(sourceAndCode[1].trim(),
					    sourceAndCode[0].trim());
					if (concept != null)
						value = concept.getUuid();
				}
				
				state = HtmlFormEntryUtil.getState(value, program);
				if (state == null) {
					throw new FormEntryException(
					        "Cannot find program work flow state with its id or uuid or associated concept as '" + value
					                + "'");
				} else if (!workflowsAndStates.add(state.getProgramWorkflow().getUuid())) {
					throw new FormEntryException("A patient cannot be in multiple states in the same workflow");
				}
				
				states.add(state);
			}
			
		}
	}
	
	/**
	 * @see org.openmrs.module.htmlformentry.element.HtmlGeneratorElement#generateHtml(org.openmrs.module.htmlformentry.FormEntryContext)
	 */
	@Override
	public String generateHtml(FormEntryContext context) {
		StringBuilder sb = new StringBuilder();
		if (dateWidget != null) {
			sb.append(dateWidget.generateHtml(context));
			if (context.getMode() != Mode.VIEW)
				sb.append(dateErrorWidget.generateHtml(context));
		}
		return sb.toString();
	}
	
	/**
	 * @see org.openmrs.module.htmlformentry.action.FormSubmissionControllerAction#handleSubmission(org.openmrs.module.htmlformentry.FormEntrySession,
	 *      javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public void handleSubmission(FormEntrySession session, HttpServletRequest submission) {
		if (session.getContext().getMode() == Mode.ENTER) {
			Date selectedDate = null;
			if (dateWidget != null) {
				selectedDate = (Date) dateWidget.getValue(session.getContext(), submission);
				List<PatientProgram> patientPrograms = Context.getProgramWorkflowService().getPatientPrograms(
				    session.getContext().getExistingPatient(), program, null, null, null, null, false);
				
				//if patient is already enrolled in program
				if (patientPrograms.size() == 1) {
					PatientProgram pp = patientPrograms.get(0);
					//if the program is not yet completed and date enrolled is after selectedDate, use selected date
					if (pp.getDateCompleted() == null
					        && OpenmrsUtil.compareWithNullAsGreatest(selectedDate, pp.getDateEnrolled()) < 0) {
						//set the start dates of all stated with a start date equal to the enrollment date to the selected date
						for (PatientState patientState : pp.getStates()) {
							if (OpenmrsUtil.nullSafeEquals(patientState.getStartDate(), pp.getDateEnrolled()))
								patientState.setStartDate(selectedDate);
						}
						pp.setDateEnrolled(selectedDate);
						session.getSubmissionActions().getPatientProgramsToEdit().add(pp);
					}
					//else: patient is enrolled in the program or the program ended, return
					
					return;
				}
			}
			
			session.getSubmissionActions().enrollInProgram(program, selectedDate, states);
		}
	}
	
	/**
	 * @see org.openmrs.module.htmlformentry.action.FormSubmissionControllerAction#validateSubmission(org.openmrs.module.htmlformentry.FormEntryContext,
	 *      javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public Collection<FormSubmissionError> validateSubmission(FormEntryContext context, HttpServletRequest submission) {
		return Collections.emptySet();
	}
	
}
