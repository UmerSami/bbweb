define([], function() {
  'use strict';

  spcLinkTypesServiceFactory.$inject = ['biobankApi'];

  /**
   * Service to access Spcecimen Link Types.
   */
  function spcLinkTypesServiceFactory(biobankApi) {
    var service = {
      getAll      : getAll,
      get         : get,
      addOrUpdate : addOrUpdate,
      remove      : remove
    };
    return service;

    //-------

    function uri(processingTypeId, ceventTypeId, version) {
      var result = '/studies';
      if (arguments.length <= 0) {
        throw new Error('study id not specified');
      } else {
        result += '/' + processingTypeId + '/sltypes';

        if (arguments.length > 1) {
          result += '/' + ceventTypeId;
        }

        if (arguments.length > 2) {
          result += '/' + version;
        }
      }
      return result;
    }

    function getAll(processingTypeId) {
      return biobankApi.get(uri(processingTypeId));
    }

    function get(processingTypeId, spcLinkTypeId) {
      return biobankApi.get(uri(processingTypeId) + '?slTypeId=' + spcLinkTypeId);
    }

    function addOrUpdate(spcLinkType) {
      var cmd = {
        processingTypeId:      spcLinkType.processingTypeId,
        expectedInputChange:   spcLinkType.expectedInputChange,
        expectedOutputChange:  spcLinkType.expectedOutputChange,
        inputCount:            spcLinkType.inputCount,
        outputCount:           spcLinkType.outputCount,
        inputGroupId:          spcLinkType.inputGroupId,
        outputGroupId:         spcLinkType.outputGroupId,
        inputContainerTypeId:  spcLinkType.inputContainerTypeId,
        outputContainerTypeId: spcLinkType.outputContainerTypeId,
        annotationTypeData:    spcLinkType.annotationTypeData
      };

      if (spcLinkType.id) {
        cmd.id = spcLinkType.id;
        cmd.expectedVersion = spcLinkType.version;
        return biobankApi.put(uri(spcLinkType.processingTypeId, spcLinkType.id), cmd);
      } else {
        return biobankApi.post(uri(spcLinkType.processingTypeId), cmd);
      }
    }

    function remove(spcLinkType) {
      return biobankApi.del(
        uri(spcLinkType.processingTypeId, spcLinkType.id, spcLinkType.version));
    }

  }

  return spcLinkTypesServiceFactory;
});
