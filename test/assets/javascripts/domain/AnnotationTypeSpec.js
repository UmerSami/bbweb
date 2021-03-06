/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'angularMocks', 'lodash', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('AnnotationType', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(testSuiteMixin, extendedDomainEntities) {
      _.extend(this, testSuiteMixin);
      this.injectDependencies('AnnotationType',
                              'AnnotationValueType',
                              'AnnotationMaxValueCount',
                              'factory');
    }));

    it('invalid objects are reported', function () {
      var self = this,
          annotationTypeJson = self.factory.annotationType({
            valueType: self.AnnotationValueType.SELECT,
            options: []
          });

       var fields = [ 'uniqueId', 'name', 'valueType', 'required' ];

      _.each(fields, function (field) {
        var jsonMissingField = _.omit(annotationTypeJson, field);
        expect(self.AnnotationType.valid(jsonMissingField)).toEqual(false);
      });
    });

    it('has default values', function() {
      var annotationType = new this.AnnotationType();

      expect(annotationType.uniqueId).toBeEmptyString();
      expect(annotationType.name).toBeEmptyString();
      expect(annotationType.description).toBeNull();
      expect(annotationType.valueType).toBeEmptyString();
      expect(annotationType.maxValueCount).toBeNull();
      expect(annotationType.options).toBeEmptyArray();
    });

    it('can be created from valid JSON', function () {
      var self = this,
          annotationTypeJson = self.factory.annotationType({
            valueType: self.AnnotationValueType.SELECT,
            options: ['opt1', 'opt2']
          }),
          annotationType = self.AnnotationType.create(annotationTypeJson);

      annotationType.compareToJsonEntity(annotationTypeJson);
    });

    it('create fails for invalid JSON', function () {
      var self = this,
          annotationTypeJson = self.factory.annotationType({
            valueType: self.AnnotationValueType.SELECT,
            options: []
          });

       var fields = [ 'uniqueId', 'name', 'valueType', 'required' ];

      _.each(fields, function (field) {
        var jsonMissingField = _.omit(annotationTypeJson, field);
        expect(function () { self.AnnotationType.create(jsonMissingField); })
          .toThrowError(/invalid object from server/);
      });
    });

    it('valueType predicates return valid results', function() {
      var self = this;

      _.each(_.values(self.AnnotationValueType), function (valueType) {
        var annotationType = new self.AnnotationType(
          self.factory.annotationType({ valueType: valueType }));

        expect(annotationType.isValueTypeText()).toBe(valueType === self.AnnotationValueType.TEXT);
        expect(annotationType.isValueTypeNumber()).toBe(valueType === self.AnnotationValueType.NUMBER);
        expect(annotationType.isValueTypeDateTime()).toBe(valueType === self.AnnotationValueType.DATE_TIME);
        expect(annotationType.isValueTypeSelect()).toBe(valueType === self.AnnotationValueType.SELECT);
      });
    });

    it('isSingleSelect returns valid results', function() {
      var self = this;

      _.each(_.range(4), function (maxValueCount) {
        var annotationType = new self.AnnotationType(
          self.factory.annotationType({
            valueType: self.AnnotationValueType.SELECT,
            maxValueCount: maxValueCount
          }));

        expect(annotationType.isSingleSelect()).toBe(
          (maxValueCount === self.AnnotationMaxValueCount.SELECT_SINGLE()));
      });
    });

    it('isMultipleSelect returns valid results', function() {
      var self = this;

      _.each(_.range(4), function (maxValueCount) {
        var annotationType = new self.AnnotationType(
          self.factory.annotationType({
            valueType: self.AnnotationValueType.SELECT,
            maxValueCount: maxValueCount
          }));

        expect(annotationType.isMultipleSelect()).toBe(
          (maxValueCount === self.AnnotationMaxValueCount.SELECT_MULTIPLE()));
      });
    });

    it('isMaxValueCountValid returns valid results', function() {
      var self = this, annotationType;

      _.each(_.range(4), function (maxValueCount) {
        annotationType = new self.AnnotationType(
          self.factory.annotationType({
            valueType: self.AnnotationValueType.SELECT,
            maxValueCount: maxValueCount
          }));

        expect(annotationType.isMaxValueCountValid()).toBe(
          (maxValueCount === self.AnnotationMaxValueCount.SELECT_SINGLE()) ||
            (maxValueCount === self.AnnotationMaxValueCount.SELECT_MULTIPLE()));
      });

      _.each(_.range(4), function (maxValueCount) {
        annotationType = new self.AnnotationType(
          self.factory.annotationType({
            valueType: self.AnnotationValueType.TEXT,
            maxValueCount: maxValueCount
          }));

        expect(annotationType.isMaxValueCountValid())
          .toBe(maxValueCount === self.AnnotationMaxValueCount.NONE());
      });
    });

    it('addOption throws an error if value type is not SELECT', function() {
      var self = this,
          valueTypesNoSelect = _.reject(_.values(self.AnnotationValueType), function (valueType) {
            return valueType === self.AnnotationValueType.SELECT;
          });

      _.each(valueTypesNoSelect, function (valueType) {
        var annotationType = new self.AnnotationType(
          self.factory.annotationType({ valueType: valueType }));

        expect(function () { annotationType.addOption(); })
          .toThrow(new Error('value type is not select: ' + valueType));
      });
    });

    it('addOption adds an item to the options array', function() {
      var self = this,
          annotationType = new self.AnnotationType(
            self.factory.annotationType({
              valueType: self.AnnotationValueType.SELECT,
              options: []
            }));
      expect(annotationType.options).toBeArrayOfSize(0);
      annotationType.addOption();
      expect(annotationType.options).toBeArrayOfSize(1);
      expect(annotationType.options[0]).toBe('');
    });

    it('removeOption throws an error if options array is empty', function() {
      var self = this,
          annotationType = new self.AnnotationType(
            self.factory.annotationType({
              valueType: self.AnnotationValueType.SELECT,
              options: []
            }));

        expect(function () { annotationType.removeOption('abc'); })
          .toThrow(new Error('options is empty, cannot remove any more options'));
    });

    it('removeOption removes an item to the options array', function() {
      var self = this,
          options = ['option1', 'option2'],
          annotationType = new self.AnnotationType(
            self.factory.annotationType({
              valueType: self.AnnotationValueType.SELECT,
              options: options.slice()
            }));
      expect(annotationType.options).toBeArrayOfSize(options.length);
      annotationType.removeOption(0);
      expect(annotationType.options).toBeArrayOfSize(options.length - 1);
      expect(annotationType.options[0]).toContain(options[1]);
    });
  });

});
