/*******************************************************************************
 * @author leolellisr
 ******************************************************************************/
package org.datavec.api.transform;

public interface Operation<TIn, TOut> {
    TOut transform(TIn input);
}
