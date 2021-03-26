package ru.transservice.routemanager.ui.routesettings

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import ru.transservice.routemanager.R
import ru.transservice.routemanager.databinding.FragmentRouteSettingsBinding
import java.text.SimpleDateFormat
import java.util.*

class RouteSettingsFragment : Fragment() {

    private var _binding: FragmentRouteSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RouteSettingsViewModel
    lateinit var navController: NavController
    private var snackbarMessage: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
        initViewModel()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRouteSettingsBinding.inflate(inflater, container, false)
        return binding.root    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        binding.tvRegion.setOnClickListener {
           navController.navigate(RouteSettingsFragmentDirections.actionRouteSettingsFragmentToRegionListFragment(viewModel.getRegion().value))
        }

        binding.tvVehicle.setOnClickListener {
            navController.navigate(RouteSettingsFragmentDirections.actionRouteSettingsFragmentToVehicleListFragment(viewModel.getVehicle().value))
        }


        var cal = Calendar.getInstance()
        cal.time = viewModel.getDate().value ?: Date()

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                viewModel.setDate(cal.time)

                val myFormat = "yyyy.MM.dd" // mention the format you need
                val sdf = SimpleDateFormat(myFormat, Locale("ru"))
                binding.tvDate.text = sdf.format(cal.time)

            }

        binding.tvDate.setOnClickListener {
            DatePickerDialog(requireContext(), dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        viewModel.getRegion().observe(viewLifecycleOwner, Observer {
            binding.tvRegion.text = it?.name
        })

        viewModel.getVehicle().observe(viewLifecycleOwner, Observer {
            binding.tvVehicle.text = it?.name
        })

        viewModel.getDate().observe(viewLifecycleOwner, Observer {
            val myFormat = "yyyy.MM.dd" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale("ru"))
            binding.tvDate.text = sdf.format(cal.time)
        })

        viewModel.getEditingIsAvailable().observe(viewLifecycleOwner, {
            with(binding.root){
                isClickable = it
                isEnabled = it
                if (this is ViewGroup){
                    this.children.forEach { childView ->
                        childView.isClickable = it
                        childView.isEnabled = it
                    }
                }
            }
            if (!binding.root.isClickable) {
                snackbarMessage = Snackbar.make(binding.root,
                        "Существует активное задание, редактирование настроек запрещено. Завершите маршрут для смены настроек",
                        Snackbar.LENGTH_INDEFINITE)
                snackbarMessage?.let {
                    val snackTextView = it.view.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
                    snackTextView.maxLines = 4
                    it.setAction(resources.getString(R.string.ok), object : View.OnClickListener{
                        override fun onClick(p0: View?) {
                            it.dismiss()
                        }
                    })
                    it.show()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        snackbarMessage?.let {
            it.dismiss()
        }
    }

    fun initViewModel(){
        viewModel  = ViewModelProvider(requireActivity(), RouteSettingsViewModel.RouteSettingsViewModelFactory()).get(
                RouteSettingsViewModel::class.java)
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            RouteSettingsFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}