package com.dmanluc.epg.data.transformer

/**
 *  Transformer interface to adapt an specific output contract from JSON response to the specific domain entity of the app
 *
 * @author   Daniel Manrique <dmanluc91@gmail.com>
 * @version  1
 * @since    2/8/18.
 */
interface Transformer<in R, out T> {

    fun transformContractToModelEntity(outputContract: R): T

}