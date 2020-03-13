$(window).load(function(){
	
	$(".desplegarEstado").append(codeLatLng(1,1)) 
	$(".inicial").removeClass("inicial").addClass("final");
	
	});
function recargarGauge(){
	$(".inicial").removeClass("inicial").addClass("final");
	}

var geocoder;
var map;
var infowindow = new google.maps.InfoWindow();
var marker;


function getAutoCompleteText(){
	var texto=$("reportes:noSerieReporte_hinput").val();
	return 	texto;
}

function codeLatLng(latitud,longitud) {
	var geocoder = new google.maps.Geocoder();
	  var latlng = new google.maps.LatLng(40.730885,-73.997383);
	  var resultado;
  geocoder.geocode({'latLng': latlng}, function(results, status) {
    if (status == google.maps.GeocoderStatus.OK) {
      if (results[1]) {
        map.setZoom(11);
        marker = new google.maps.Marker({
            position: latlng,
            map: map
        });
       resultado= results[1].formatted_address;
      }
    } else {
      alert("Geocoder failed due to: " + status);
    }
  });
  return resultado;
}
function dataFilterEvent(idCajaFechaCalendario, idCajaFechaInputFilter) {
	var fechaInicioCapturada = $(idCajaFechaCalendario).val();
	$(idCajaFechaInputFilter).val(fechaInicioCapturada);
	resumenTable.filter();
};