package ru.transservice.routemanager.ui.point

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ru.transservice.routemanager.AppClass
import ru.transservice.routemanager.data.local.entities.*
import ru.transservice.routemanager.repositories.RootRepository
import ru.transservice.routemanager.ui.task.TaskListViewModel
import ru.transservice.routemanager.utils.ImageFileProcessing
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*

class PointItemViewModel(pointId: String) : ViewModel() {

    private val repository = RootRepository
    val state: LiveData<PointWithData> = repository.observePointItemState(pointId).asLiveData()
    private val currentTask = repository.observeTask().asLiveData()
    val currentPoint: PointItem? get() = state.value?.let { it.point }
    var reasonComment: String = ""

    //TODO redo the algorithm with geoless files

    var geoIsRequired = MutableLiveData(false)
    companion object {
        private const val TAG = "${AppClass.TAG}: TaskList_View_Model"
    }

    fun initPointData() {
        updateGeoIsRequired()
        if (reasonComment.isEmpty()) {
            reasonComment = state.value?.point?.reasonComment ?: ""
        }
    }

    fun savePointFile(file: File, location: Location?, fileOrder: PhotoOrder){

        location?.let {
            ImageFileProcessing.setGeoTag(location, file.absolutePath)
        }

        val exifInterface = androidx.exifinterface.media.ExifInterface(file.absoluteFile)
        val latLon = exifInterface.latLong
        var lat = 0.0
        var lon = 0.0
        if (latLon != null) {
            lat = latLon[0]
            lon = latLon[1]
        } else {
            Log.d(TAG, "location is not defined ${file.absolutePath}")
            //Toast.makeText(getApplication(), "Предупреждение, местоположение не определено", Toast.LENGTH_LONG).show()
        }

        val pointFile = PointFile(
            state.value!!.point.docUID, state.value!!.point.lineUID, Date(file.lastModified()), fileOrder,
            lat,
            lon,
            file.absolutePath, file.name, file.extension
        )

        repository.insertPointFile(pointFile) {
            repository.uploadFile(pointFile)

            if (fileOrder == PhotoOrder.PHOTO_AFTER || fileOrder == PhotoOrder.PHOTO_CANTDONE) {
                state.value?.let { it ->
                    val resultPoint = it.point.copy()
                    updatePointAndDoneStatus(resultPoint)
                }
            }
        }
    }

    fun updateCurrentPoint(pointItem: PointItem){
        //TODO check online updating
        //currentPoint.postValue(pointItem)
        repository.updatePoint(pointItem)
        repository.updatePointOnServer(pointItem)
    }

    fun updateUndonePoint(){
        /*currentPoint.value?.let { pointItem ->
            val resultPoint = pointItem.copy(reasonComment =  reasonComment, tripNumberFact = 2000)
            updatePointAndDoneStatus(resultPoint)
        }*/
        state.value?.let{ pointState ->
            val resultPoint =  pointState.point
                .copy(reasonComment =  reasonComment, tripNumberFact = 2000)
                .also {
                    if (it.timestamp == null){
                        it.timestamp = Date()
                    }
                    it.status = PointStatuses.CANNOT_DONE
                }
            updateCurrentPoint(resultPoint)
        }
    }

    fun setPointFilesGeodata(location: Location) {
        state.value?.let{ currentState ->
            viewModelScope.launch {
                repository.getGeolessPointFiles(currentState.point) { list ->
                    list.forEach {
                        if (it.filePath.isNotEmpty()) {
                            val lon = location.longitude
                            val lat = location.latitude
                            ImageFileProcessing.createResultImageFile(
                                it.filePath, lat, lon, currentState.toPointFileParams(it.photoOrder),
                                AppClass.appliactionContext()
                            )
                            ImageFileProcessing.setGeoTag(location, it.filePath)
                            repository.updatePointFileLocation(it, lat, lon) {
                                Log.d(
                                    TAG,
                                    "update point file location, point file: ${it.filePath}, lat: $lat, lon: $lon"
                                )
                            }
                        }
                    }
                    updateGeoIsRequired()
                }
            }
        }

    }

    fun setFact(fact: Double){
        state.value?.let { pointState ->
            val resultPoint = pointState.point.copy(countFact = fact)
            resultPoint.setCountOverFromPlanAndFact()
            updatePointAndDoneStatus(resultPoint)
        }
    }

    fun setPolygonForPoint(polygon: PolygonItem) {
        state.value?.let { pointState ->
            val resultPoint = pointState.point.copy(polygonUID = polygon.uid, polygonName = polygon.name)
            updatePointAndDoneStatus(resultPoint)
        }
    }

    fun getPhoneNumber() : String{
        return state.value?.point?.getPhoneFromComment() ?: ""
    }

    private fun updateGeoIsRequired(){
        state.value?.let {
            repository.getGeolessPointFiles(it.point) { list ->
                geoIsRequired.postValue(list.isNotEmpty())
            }
        }
    }

    private fun updatePointAndDoneStatus(point: PointItem) {
        repository.checkPointForCompletion(point) { canBeDone ->
            val statusChanged = point.done != canBeDone
            point.done = canBeDone
            if (statusChanged) {
                point.timestamp = Date()
                if (reasonComment != "") {
                    point.reasonComment = reasonComment
                }
            }
            when {
                point.done ->
                    point.status = PointStatuses.DONE
                !point.done && point.countFact != 0.0 && point.countFact != -1.0 ->
                    point.status = PointStatuses.NOT_VISITED
                !point.done && point.countFact == 0.0 ->
                    point.status = PointStatuses.CANNOT_DONE
                !point.done && point.reasonComment != "" ->
                    point.status = PointStatuses.CANNOT_DONE
            }

            if (point.done && point.tripNumberFact == 2000)
                point.tripNumberFact = 1000
            if (point.done && point.polygonByRow && point.tripNumberFact >= 1000)
                currentTask.value?.let {
                    point.tripNumberFact = it.task.lastTripNumber + 1
                }
            updateCurrentPoint(point)
        }
    }

}

class PointItemViewModelFactory(val pointId: String): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(PointItemViewModel::class.java)) {
            PointItemViewModel(pointId) as T
        }else{
            throw IllegalArgumentException("Unknown class: Expected ${this::class.java} found $modelClass")
        }
    }

}