/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.transservice.routemanager.ui.camera

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import ru.transservice.routemanager.R
import com.bumptech.glide.Glide
import ru.transservice.routemanager.AppClass
import ru.transservice.routemanager.MainActivity
import ru.transservice.routemanager.data.local.entities.PointItem
import ru.transservice.routemanager.data.local.entities.PointStatuses
import ru.transservice.routemanager.location.NavigationServiceConnection
import ru.transservice.routemanager.ui.task.TaskListViewModel
import ru.transservice.routemanager.utils.ImageFileProcessing
import java.io.File

class PhotoFragment : Fragment() {

    private var currentFile: File? = null
    private lateinit var viewPointModel: TaskListViewModel
    private lateinit var point: PointItem
    private lateinit var pointAction: PointStatuses

    lateinit var navController: NavController
    private val args: PhotoFragmentArgs by navArgs()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // get the same viewModel as point_action fragment
        currentFile = File(args.fileName)
        point = args.point
        pointAction = args.pointAction
        //gps = GPSTracker.getGPSTracker(requireActivity())
        //gps = GPSTracker(requireContext())
        if (currentFile == null) {
            Toast.makeText(requireContext(), "Ошибка получения фото, неверное имя файла",Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
        initViewModel()
        Log.d(TAG, "current file: ${currentFile?.absolutePath}")
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_photo_priview, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        val resource = currentFile ?: R.drawable.ic_photo
        val imageView = view.findViewById<ImageView>(R.id.photoPreview)
        //location
        /*if (NavigationServiceConnection.getlocationAvailable()) {
        }else{
        }*/
        val location: Location? = NavigationServiceConnection.getLocation()
        if (location == null) {
            viewPointModel.geoIsRequired.value = true
        }else{
            Log.d(TAG, "location successfully requested lat: ${location.latitude} lon: ${location.longitude}")
        }

        //For testing null location
        // location = null
        //viewPointModel.geoIsRequired.value = true
        if (currentFile!=null && location!=null){
            ImageFileProcessing.createResultImageFile(currentFile!!.absolutePath,location.latitude,location.longitude,point,requireContext())
        }

        Glide.with(requireContext()).load(resource).into(imageView as ImageView)
        view.findViewById<TextView>(R.id.tv_confirm).setOnClickListener {
            currentFile?.let {
                viewPointModel.savePointFile(it,location)
            }
            navController.navigate(PhotoFragmentDirections.actionPhotoFragmentToPointFragment(point,pointAction))
        }

        view.findViewById<TextView>(R.id.tv_cancel).setOnClickListener {
            currentFile?.delete()
            navController.popBackStack()
        }

        (requireActivity() as MainActivity).supportActionBar?.hide()
    }

    fun initViewModel(){
        viewPointModel = ViewModelProvider(requireActivity(), TaskListViewModel.TaskListViewModelFactory(requireActivity().application)).get(TaskListViewModel::class.java)
    }

    companion object {
        private const val TAG = "${AppClass.TAG}: Photo_Preview"
    }
}

