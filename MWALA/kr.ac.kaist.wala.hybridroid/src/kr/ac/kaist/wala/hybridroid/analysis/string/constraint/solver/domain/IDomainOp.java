/*******************************************************************************
* Copyright (c) 2016 IBM Corporation and KAIST.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* KAIST - initial API and implementation
*******************************************************************************/
package kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain;

import kr.ac.kaist.wala.hybridroid.analysis.string.constraint.solver.domain.value.IValue;

import java.util.Set;

public interface IDomainOp<S, T> {
	public IValue alpha(S cv);
	public IValue alpha(Set<S> cv);
	public T gamma(IValue v);
}
