package ru.transservice.routemanager.ui.task

import android.util.Log
import androidx.lifecycle.*
import ru.transservice.routemanager.AppClass
import ru.transservice.routemanager.data.local.entities.*
import ru.transservice.routemanager.repositories.RootRepository

class TaskListViewModel : ViewModel() {

    private val repository = RootRepository
    private var pointList : LiveData<List<PointItem>> = repository.observePointList().asLiveData()

    var unloadingAvailable = Transformations.map(
        repository.getUnloadingAvailable()
    ){
        it
    }

    private var currentPoint: MutableLiveData<PointItem> = MutableLiveData()
    private val query = MutableLiveData("")
    private val fullList = MutableLiveData(true)

    val mediatorListResult = MediatorLiveData<List<PointItem>>()

    fun getCurrentPoint(): MutableLiveData<PointItem>{
        return currentPoint
    }

    fun setCurrentPoint(point: PointItem){
        currentPoint.value = point
        Log.d(TAG,"Current point changed ${currentPoint.value}")
    }

    fun updateCurrentPoint(pointItem: PointItem){
        currentPoint.postValue(pointItem)
        repository.updatePoint(pointItem)
        repository.updatePointOnServer(pointItem)
    }


    //Delete polygon from the list and clear points with this polygon
    fun deletePolygonFromList() {
        currentPoint.value?.let {
            repository.deletePolygon(it)
        }
    }

    fun handleSearchQuery(text: String) {
        query.value = text
    }

    fun setFullList(value: Boolean){
        fullList.value = value
    }

    fun changeFullList(): Boolean {
        fullList.value = ! (fullList.value?: false)
        return fullList.value ?: true
    }

    fun addSources(){
        val filterF = {
            val queryStr = query.value!!
            val points: List<PointItem> =
                if (pointList.value == null) listOf() else pointList.value!!
            mediatorListResult.value = when {
                queryStr.isNotEmpty() && fullList.value == false -> points
                    .filter { it.addressName.contains(queryStr, true) }
                    .filter { !it.done }
                queryStr.isNotEmpty() && fullList.value == true -> points
                    .filter { it.addressName.contains(queryStr, true) }
                queryStr.isEmpty() && fullList.value == false -> points
                    .filter { !it.done }
                else -> points
            }
        }

        mediatorListResult.addSource(pointList) { filterF.invoke() }
        mediatorListResult.addSource(query) { filterF.invoke() }
        mediatorListResult.addSource(fullList) { filterF.invoke() }
    }

    fun removeSources(){
        mediatorListResult.removeSource(pointList)
        mediatorListResult.removeSource(query)
        mediatorListResult.removeSource(fullList)
    }

    companion object {
        private const val TAG = "${AppClass.TAG}: TaskList_View_Model"
    }
}