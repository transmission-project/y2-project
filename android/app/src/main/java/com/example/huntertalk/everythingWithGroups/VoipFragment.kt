package com.example.huntertalk.everythingWithGroups

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast

import com.example.huntertalk.R
import kotlinx.android.synthetic.main.fragment_voip.*

// Permission request callback code
private const val MICROPHONE_REQUEST = 1888

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [VoipFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [VoipFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class VoipFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = activity!!.applicationContext
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }


        //startVoiceWebView()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_voip, container, false)

        startVoiceWebView()

        return rootView
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }

        checkAndRequestMicPerms(context)
    }

    private fun checkAndRequestMicPerms(context: Context) {
        /**
         * Check for microphone permissions and request them.
         *
         * This is needed as permission.RECORD_AUDIO is a "dangerous permission" which requires the
         * user to manually approve in-app.
         */
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), MICROPHONE_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        /**
         * Callback function after permission has been requested.
         *
         * We see the result of requesting permissions here
         */
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MICROPHONE_REQUEST -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // If permission request is cancelled or denied, complain to the user and exit the fragment
                    Toast.makeText(
                            activity, getString(R.string.mic_permission_complain), Toast.LENGTH_LONG
                    ).show()
                    activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment voipFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                VoipFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }

    private fun startVoiceWebView() {
        voice_webview.settings.setJavaScriptEnabled(true)
        voice_webview.settings.setAllowUniversalAccessFromFileURLs(true)
        voice_webview.settings.setMediaPlaybackRequiresUserGesture(false)

        WebView.setWebContentsDebuggingEnabled(true) //TODO: disable this for production

        voice_webview.webChromeClient = object : WebChromeClient() {
            // Pass JS console messages to Logcat
            override fun onConsoleMessage(m: ConsoleMessage): Boolean {
                Log.d("getUserMedia, WebView", m.message() + " -- From line "
                        + m.lineNumber() + " of "
                        + m.sourceId())
                return true
            }

            //Auto-accept webRTC media request
            override fun onPermissionRequest(request: PermissionRequest) {
                activity?.runOnUiThread(Runnable { request.grant(request.resources) })
            }
        }

        voice_webview.loadUrl("file:///android_asset/index.html")
        // TODO: JS interface for choosing group to connect to, push to talk, stream activity
    }
}
