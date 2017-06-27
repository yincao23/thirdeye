function renderMappingsSelection() {
  var html = "<table style='border: 1px;width: 80%'><tr><td>Select entity 1 </td><td><select name='entityTypeSelector1' id='entityTypeSelector1' onchange='renderEntityTypeSelector(this.id)'></select><div id='entityTypeSelector1_details'></div><br/>is affected by:<br/></td></tr>"
      + "<tr><td>Select entity 2 </td><td><select name='entityTypeSelector2' id='entityTypeSelector2' onchange='renderEntityTypeSelector(this.id)'></select><div id='entityTypeSelector2_details'></div></td></tr>"
      + "</table>" + "<input type='submit' onclick='updateEntityMapping()' />"
      + "<hr/><div id='existing_mappings'></div>"
      + "<hr/><table id='existing-mappings-data-table'></table>";
  $("#mappings-place-holder").html(html);

  getData("/data/entityTypes", "admin").done(function (data) {
    var select = "<option value='select'>Select</option>";
    for (var i in data) {
      select += "<option value='" + data[i] + "'>" + data[i] + "</option>";
    }
    $("#entityTypeSelector1").html(select);
    $("#entityTypeSelector2").html(select);
  });
}

function renderEntityTypeSelector(divId) {
  var entityType = $("#" + divId).find(':selected').val();
  $("#" + divId + "_details").html(getEntitySpecificSelectionOptions(divId, entityType));

  var entitySelect = $("#" + divId + "_" + entityType);
  var entityUrn = $("#" + divId + "_" + entityType + "_urn");

  if (entityType === 'METRIC') {
    entitySelect.select2({
      ajax: {
        url: "/data/autocomplete/metric", delay: 250, data: function (params) {
          var query = {
            name: params.term
          };
          // Query parameters will be ?name=[term]&page=[page]
          return query;
        }, processResults: function (data) {
          var results = [];
          $.each(data, function (index, item) {
            results.push({
              id: item.id, text: item.alias, name: item.name
            });
          });
          return {
            results: results
          };
        }
      }
    });
    entitySelect.on("change", function (e) {
      const $selectedElement = $(e.currentTarget);
      const selectedData = $selectedElement.select2('data');
      var metricId = "";
      if (selectedData.length) {
        const {id, text, name} = selectedData[0];
        metricId = id;
        metricAlias = text;
        metricName = name;
      }
      entityUrn.val(getUrnForPrefixEntityType(entityType) + metricId);
    });
  } else {
    entityUrn.val(getUrnForPrefixEntityType(entityType));
  }
}

function getUrnForPrefixEntityType(entityType) {
  switch (entityType) {
    case "METRIC":
      return "thirdeye:metric:";
    case "DIMENSION":
    case "DIMENSION_VAL":
      return "thirdeye:dimension:";
    default:
      return "thirdeye:";
  }
}

function getEntitySpecificSelectionOptions(baseId, entityType) {
  var html = "";

  var suffixHtml = "";
  if (baseId == "entityTypeSelector1") {
    suffixHtml = "<a href='#' onclick='fetchAndDisplayMappings(\"" + entityType
        + "\")'> refresh</a>";
  }

  switch (entityType) {
    case "METRIC":
      html += "<select style='width:100%' id='" + baseId + "_" + entityType + "'></select><div id='"
          + baseId + "_final_urn' ></div>";
      break;
    case "DIMENSION":
    case "DIMENSION_VAL":
    default:
      // do nothing
  }
  html += "<input style='width:100%' type='text' placeholder='provide urn' id='" + baseId + "_"
      + entityType + "_urn'>" + suffixHtml;
  return html;
}

function updateEntityMapping() {
  var entityType1 = $("#entityTypeSelector1").find(':selected').val();
  var entityType2 = $("#entityTypeSelector2").find(':selected').val();
  var fromUrn = $("#entityTypeSelector1_" + entityType1 + "_urn").val();
  var toUrn = $("#entityTypeSelector2_" + entityType2 + "_urn").val();

  console.log(entityType1)
  console.log(entityType2)
  console.log(fromUrn)
  console.log(toUrn)

  var payload = '{ "fromURN": "' + fromUrn + '",  "toURN": "' + toUrn + '", "mappingType": "'
      + entityType1 + "_TO_" + entityType2 + '"}';

  if (entityType1 == 'select' || entityType2 == 'select' || fromUrn == '' || toUrn == '') {
    return;
  }

  submitData("/entityMapping/create", payload, "admin").done(function (data) {
    console.log("Adding mapping : ")
    console.log(payload)
  });

  fetchAndDisplayMappings(entityType1);
}

function fetchAndDisplayMappings(entityType) {
  console.log("fetching and displaying data for " + entityType);
  var fromUrn = $("#entityTypeSelector1_" + entityType + "_urn").val();
  if (fromUrn == '') {
    return;
  }

  getData("/entityMapping/view/fromURN/" + fromUrn, "admin").success(function (urns) {
    const urn2id = {};
    for (var i = 0; i < urns.length; i++) {
      const u = urns[i];
      urn2id[u.toURN] = u.id;
    }
    console.log(urn2id);

    const urnsString = urns.map(function (x) {
      return (x.toURN)
    }).join(",");
    console.log(urnsString);

    if ($.fn.dataTable.isDataTable( '#existing-mappings-data-table' )) {
      // existing table
      const table = $('#existing-mappings-data-table').DataTable();
      table.destroy();
      $('#existing-mappings-data-table').empty();
    }

    // new table
    $('#existing-mappings-data-table').DataTable({
      ajax: {
        url: '/rootcause/raw?framework=identity&urns=' + urnsString, dataSrc: ''
      }, columns: [{
        title: 'Type', data: null, render: function (data, type, row) {
          return ("<a href=\"" + row.link + "\" target=\"_blank\">" + row.type + "</a>");
        }
      }, {
        title: 'toURN', data: 'urn'
      }, {
        title: 'Label', data: 'label'
      }, {
        title: '', data: null, render: function (data, type, row) {
          return ("<a href=\"#\" onclick=\"deleteMapping(\'" + urn2id[row.urn] + "\')\">delete</a>");
        }
      }], order: [[3, 'desc'], [2, 'asc'], [1, 'asc']], iDisplayLength: 50, retrieve: true
    });

  });
}

function deleteMapping(id) {
  deleteData("/entityMapping/delete/" + id, "", "mapping", "html").success(function (data) {
    console.log("deleted mapping with id : " + id);
    const entityType1 = $("#entityTypeSelector1").find(':selected').val();
    fetchAndDisplayMappings(entityType1);
  });
}
