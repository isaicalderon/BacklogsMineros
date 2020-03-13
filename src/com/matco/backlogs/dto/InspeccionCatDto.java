package com.matco.backlogs.dto;

import java.io.Serializable;

import com.matco.amce3.dto.InspeccionDto;
import com.matco.backlogs.entity.BacklogsMineros;

public class InspeccionCatDto implements Serializable {

	private static final long serialVersionUID = 714127458074417380L;

	private boolean seGuardoCorrectamente = false;
	private InspeccionDto inspeccionDto;
	private BacklogsMineros backlog = new BacklogsMineros();;

	public boolean isSeGuardoCorrectamente() {
		return seGuardoCorrectamente;
	}

	public void setSeGuardoCorrectamente(boolean seGuardoCorrectamente) {
		this.seGuardoCorrectamente = seGuardoCorrectamente;
	}

	public InspeccionDto getInspeccionDto() {
		return inspeccionDto;
	}

	public void setInspeccionDto(InspeccionDto inspeccionDto) {
		this.inspeccionDto = inspeccionDto;
	}

	public BacklogsMineros getBacklog() {
		return backlog;
	}

	public void setBacklog(BacklogsMineros backlog) {
		this.backlog = backlog;
	}

}
