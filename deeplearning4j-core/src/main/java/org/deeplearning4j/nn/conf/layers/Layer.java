/*
 *
 *  * Copyright 2015 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package org.deeplearning4j.nn.conf.layers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.distribution.Distribution;
import org.deeplearning4j.nn.layers.normalization.LocalResponseNormalization;
import org.deeplearning4j.nn.weights.WeightInit;

/**
 * A neural network layer.
 */
@JsonTypeInfo(use=Id.NAME, include=As.WRAPPER_OBJECT)
@JsonSubTypes(value={
        @JsonSubTypes.Type(value = AutoEncoder.class, name = "autoEncoder"),
        @JsonSubTypes.Type(value = ConvolutionLayer.class, name = "convolution"),
        @JsonSubTypes.Type(value = ImageLSTM.class, name = "imageLSTM"),
        @JsonSubTypes.Type(value = GravesLSTM.class, name = "gravesLSTM"),
        @JsonSubTypes.Type(value = GRU.class, name = "gru"),
        @JsonSubTypes.Type(value = OutputLayer.class, name = "output"),
        @JsonSubTypes.Type(value = RnnOutputLayer.class, name = "rnnoutput"),
        @JsonSubTypes.Type(value = RBM.class, name = "RBM"),
        @JsonSubTypes.Type(value = DenseLayer.class, name = "dense"),
        @JsonSubTypes.Type(value = RecursiveAutoEncoder.class, name = "recursiveAutoEncoder"),
        @JsonSubTypes.Type(value = SubsamplingLayer.class, name = "subsampling"),
        @JsonSubTypes.Type(value = LocalResponseNormalization.class, name = "localResponseNormalization"),
        })
@Data
@NoArgsConstructor
public abstract class Layer implements Serializable, Cloneable {
    protected String activationFunction;
    protected WeightInit weightInit;
    protected double biasInit;
    protected Distribution dist;
    protected double learningRate;
    //learning rate after n iterations
    protected Map<Integer,Double> learningRateAfter;
    protected double lrScoreBasedDecay;
    protected double momentum;
    //momentum after n iterations
    protected Map<Integer,Double> momentumAfter;
    protected double l1;
    protected double l2;
    protected double dropOut;
    protected Updater updater;
    //adadelta - weight for how much to consider previous history
    protected double rho;
    protected double rmsDecay;
    protected double adamMeanDecay = 0.9;
    protected double adamVarDecay = 0.999;
    protected GradientNormalization gradientNormalization = GradientNormalization.None; //Clipping, rescale based on l2 norm, etc
    protected double gradientNormalizationThreshold = 1.0;   //Threshold for l2 and element-wise gradient clipping

    public Layer(Builder builder) {
    	this.activationFunction = builder.activationFunction;
    	this.weightInit = builder.weightInit;
        this.biasInit = builder.biasInit;
    	this.dist = builder.dist;
        this.learningRate = builder.learningRate;
        this.learningRateAfter = builder.learningRateAfter;
        this.lrScoreBasedDecay = builder.lrScoreBasedDecay;
        this.momentum = builder.momentum;
        this.momentumAfter = builder.momentumAfter;
        this.l1 = builder.l1;
        this.l2 = builder.l2;
        this.dropOut = builder.dropOut;
        this.updater = builder.updater;
        this.rho = builder.rho;
        this.rmsDecay = builder.rmsDecay;
        this.adamMeanDecay = builder.adamMeanDecay;
        this.adamVarDecay = builder.adamVarDecay;
        this.gradientNormalization = builder.gradientNormalization;
        this.gradientNormalizationThreshold = builder.gradientNormalizationThreshold;
    }

    @Override
    public Layer clone() {
        try {
            Layer clone = (Layer) super.clone();
            if(clone.dist != null) clone.dist = clone.dist.clone();
            if(clone.momentumAfter != null) clone.momentumAfter = new HashMap<>(clone.momentumAfter);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }


    public abstract static class Builder<T extends Builder<T>> {
        protected String activationFunction = null;
        protected WeightInit weightInit = null;
        protected double biasInit = Double.NaN;
        protected Distribution dist = null;
        protected double learningRate = Double.NaN;
        protected Map<Integer,Double> learningRateAfter = null;
        protected double lrScoreBasedDecay = Double.NaN;
        protected double momentum = Double.NaN;
        protected Map<Integer,Double> momentumAfter = null;
        protected double l1 = Double.NaN;
        protected double l2 = Double.NaN;
        protected double dropOut = Double.NaN;
        protected Updater updater = null;
        protected double rho = Double.NaN;
        protected double rmsDecay = Double.NaN;
        protected double adamMeanDecay = Double.NaN;
        protected double adamVarDecay = Double.NaN;
        protected GradientNormalization gradientNormalization = null;
        protected double gradientNormalizationThreshold = Double.NaN;

        /**Layer activation function.
         * Typical values include:<br>
         * "relu" (rectified linear), "tanh", "sigmoid", "softmax",
         * "hardtanh", "leakyrelu", "maxout", "softsign", "softplus"
         */
        public T activation(String activationFunction) {
            this.activationFunction = activationFunction;
            return (T) this;
        }

        /** Weight initialization scheme.
         * @see org.deeplearning4j.nn.weights.WeightInit
         */
        public T weightInit(WeightInit weightInit) {
            this.weightInit = weightInit;
            return (T) this;
        }

        public T biasInit(double biasInit) {
            this.biasInit = biasInit;
            return (T) this;
        }

        /** Distribution to sample initial weights from. Used in conjunction with
         * .weightInit(WeightInit.DISTRIBUTION).
         */
        public T dist(Distribution dist){
        	this.dist = dist;
        	return (T) this;
        }

        /** Learning rate. Defaults to 1e-1*/
        public T learningRate(double learningRate){
            this.learningRate = learningRate;
            return (T)this;
        }

        /** Learning rate schedule. Map of the iteration to the learning rate to apply at that iteration. */
        public T learningRateAfter(Map<Integer, Double> learningRateAfter) {
            this.learningRateAfter = learningRateAfter;
            return (T) this;
        }

        /** Rate to decrease learningRate by when the score stops improving.
         * Learning rate is multiplied by this rate so ideally keep between 0 and 1. */
        public T learningRateScoreBasedDecayRate(double lrScoreBasedDecay) {
            this.lrScoreBasedDecay = lrScoreBasedDecay;
            return (T) this;
        }

        /** L1 regularization coefficient.*/
        public T l1(double l1){
            this.l1 = l1;
            return (T)this;
        }

        /** L2 regularization coefficient. */
        public T l2(double l2){
            this.l2 = l2;
            return (T)this;
        }

        public T dropOut(double dropOut) {
            this.dropOut = dropOut;
            return (T) this;
        }

        /** Momentum rate. */
        public T momentum(double momentum) {
            this.momentum = momentum;
            return (T)this;
        }

        /** Momentum schedule. Map of the iteration to the momentum rate to apply at that iteration. */
        public T momentumAfter(Map<Integer, Double> momentumAfter) {
            this.momentumAfter = momentumAfter;
            return (T) this;
        }

        /** Gradient updater. For example, SGD for standard stochastic gradient descent, NESTEROV for Nesterov momentum,
         * RSMPROP for RMSProp, etc.
         * @see org.deeplearning4j.nn.conf.Updater
         */
        public T updater(Updater updater){
            this.updater = updater;
            return (T) this;
        }

        /**
         * Ada delta coefficient
         * @param rho
         */
        public T rho(double rho) {
            this.rho = rho;
            return (T) this;
        }

        /** Decay rate for RMSProp. Only applies if using .updater(Updater.RMSPROP)
         */
        public T rmsDecay(double rmsDecay) {
            this.rmsDecay = rmsDecay;
            return (T) this;
        }

        /** Mean decay rate for Adam updater. Only applies if using .updater(Updater.ADAM) */
        public T adamMeanDecay(double adamMeanDecay) {
            this.adamMeanDecay = adamMeanDecay;
            return (T) this;
        }

        /** Variance decay rate for Adam updater. Only applies if using .updater(Updater.ADAM) */
        public T adamVarDecay(double adamVarDecay) {
            this.adamVarDecay = adamVarDecay;
            return (T) this;
        }

        /** Gradient normalization strategy. Used to specify gradient renormalization, gradient clipping etc.
         * @param gradientNormalization Type of normalization to use. Defaults to None.
         * @see org.deeplearning4j.nn.conf.GradientNormalization
         */
        public T gradientNormalization(GradientNormalization gradientNormalization ){
            this.gradientNormalization = gradientNormalization;
            return (T) this;
        }

        /** Threshold for gradient normalization, only used for GradientNormalization.ClipL2PerLayer,
         * GradientNormalization.ClipL2PerParamType, and GradientNormalization.ClipElementWiseAbsoluteValue<br>
         * Not used otherwise.<br>
         * L2 threshold for first two types of clipping, or absolute value threshold for last type of clipping.
         */
        public T gradientNormalizationThreshold(double threshold){
            this.gradientNormalizationThreshold = threshold;
            return (T) this;
        }

        public abstract <E extends Layer> E build();
    }
}
