package ru.netology.nework.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nework.R
import ru.netology.nework.adapters.EventAdapter
import ru.netology.nework.adapters.OnInteractionListenerEvent
import ru.netology.nework.adapters.UsersAdapter
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.auxiliary.Companion.Companion.eventId
import ru.netology.nework.auxiliary.Companion.Companion.eventRequestType
import ru.netology.nework.auxiliary.Companion.Companion.textArg
import ru.netology.nework.auxiliary.FloatingValue.currentFragment
import ru.netology.nework.databinding.FragmentEventsBinding
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.EventResponse
import ru.netology.nework.dto.User
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.EventViewModel
import ru.netology.nework.viewmodel.UsersViewModel
import javax.inject.Inject

@AndroidEntryPoint
class EventsFragment : Fragment() {

    val viewModel: EventViewModel by activityViewModels()

    val usersViewModel: UsersViewModel by viewModels()

    val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var appAuth: AppAuth


    private val interactionListener = object : OnInteractionListenerEvent {

        override fun onLike(event: EventResponse) {
            if (authViewModel.authenticated) {
                viewModel.likeById(event)
            } else {
                AlertDialog.Builder(context)
                    .setMessage(R.string.action_not_allowed)
                    .setPositiveButton(R.string.sign_up) { _, _ ->
                        findNavController().navigate(
                            R.id.action_eventsFragment_to_authFragment,
                            Bundle().apply {
                                textArg = getString(R.string.sign_up)
                            }
                        )
                    }
                    .setNeutralButton(R.string.sign_in) { _, _ ->
                        findNavController().navigate(
                            R.id.action_eventsFragment_to_authFragment,
                            Bundle().apply {
                                textArg = getString(R.string.sign_in)
                            }
                        )
                    }
                    .setNegativeButton(R.string.no, null)
                    .setCancelable(true)
                    .create()
                    .show()
            }
        }

        override fun onShare(event: EventResponse) {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, event.content)
            }

            val shareIntent =
                Intent.createChooser(intent, getString(R.string.chooser_share_post))
            startActivity(shareIntent)
        }

        override fun onRemove(event: EventResponse) {
            viewModel.removeById(event.id)
        }

        override fun onEdit(event: EventResponse) {
            viewModel.edit(event)
            findNavController().navigate(R.id.action_eventsFragment_to_newEventFragment)
        }

        override fun onPlayPost(event: EventResponse, videoView: VideoView?) {
            if (event.attachment?.type == AttachmentType.VIDEO) {
                videoView?.isVisible = true
                val uri = Uri.parse(event.attachment.url)
                videoView?.apply {
                    setMediaController(MediaController(requireContext()))
                    setVideoURI(uri)
                    setOnPreparedListener {
                        videoView.layoutParams?.height =
                            (resources.displayMetrics.widthPixels * (it.videoHeight.toDouble() / it.videoWidth)).toInt()
                        start()
                    }
                    setOnCompletionListener {

                        if (videoView.layoutParams?.width != null) {
                            videoView.layoutParams?.width = resources.displayMetrics.widthPixels
                            videoView.layoutParams?.height =
                                (videoView.layoutParams?.width!! * 0.5625).toInt()
                        }
                        stopPlayback()

                    }

                }
            }
            if (event.attachment?.type == AttachmentType.AUDIO) {
                viewModel.mediaPlayer.reset()
                if (viewModel.mediaPlayer.isPlaying) {
                    viewModel.mediaPlayer.stop()
                } else {
                    viewModel.mediaPlayer.setDataSource(event.attachment.url)
                    viewModel.mediaPlayer.prepare()
                    viewModel.mediaPlayer.start()
                }
            }
        }

        override fun onLink(event: EventResponse) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.link))
            startActivity(intent)
        }

        override fun onPreviewAttachment(event: EventResponse) {
            findNavController().navigate(
                R.id.action_eventsFragment_to_viewImageAttach,
                Bundle().apply {
                    textArg = event.attachment?.url
                })
        }

        override fun onSpeakersAction(event: EventResponse) {
            if (event.speakerIds.isNotEmpty()) {
                findNavController().navigate(
                    R.id.action_eventsFragment_to_bottomSheetFragment,
                    Bundle().apply {
                        eventId = event.id
                        eventRequestType = "speakers"
                    }
                )
            } else {
                Toast.makeText(requireContext(), R.string.not_value_event, Toast.LENGTH_SHORT)
                    .show()
            }

        }

        override fun onPartyAction(event: EventResponse) {
            if (event.participantsIds.isNotEmpty()) {
                findNavController().navigate(
                    R.id.action_eventsFragment_to_bottomSheetFragment,
                    Bundle().apply {
                        eventId = event.id
                        eventRequestType = "party"
                    }
                )
            } else {
                Toast.makeText(requireContext(), R.string.not_value_event, Toast.LENGTH_SHORT)
                    .show()
            }
        }

        override fun onJoinAction(event: EventResponse) {
            viewModel.joinById(event)
        }
    }

    private lateinit var binding: FragmentEventsBinding
    private lateinit var adapter: EventAdapter
    private lateinit var adapterUsers: UsersAdapter

    var users: List<User> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentEventsBinding.inflate(layoutInflater)
        adapter = EventAdapter(interactionListener)
        adapterUsers = UsersAdapter()

        binding.list.adapter = adapter
        binding.setUsers.adapter = adapterUsers

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                adapter.submitList(it)
            }
        }

        lifecycleScope.launchWhenCreated {
            usersViewModel.dataUsersList.collectLatest {
                users = it
                //adapterUsers.submitList(it)
            }
        }

        var menuProvider: MenuProvider? = null

        authViewModel.data.observe(viewLifecycleOwner) {
            menuProvider?.let(requireActivity()::removeMenuProvider)
            requireActivity().addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_main, menu)

                    menu.setGroupVisible(R.id.unauthenticated, !authViewModel.authenticated)
                    menu.setGroupVisible(R.id.authenticated, authViewModel.authenticated)

                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.signin -> {
                            findNavController().navigate(
                                R.id.action_feedFragment_to_authFragment,
                                Bundle().apply {
                                    textArg = getString(R.string.sign_in)
                                }
                            )
                            true
                        }
                        R.id.signup -> {
                            findNavController().navigate(
                                R.id.action_feedFragment_to_authFragment,
                                Bundle().apply {
                                    textArg = getString(R.string.sign_up)
                                }
                            )
                            true
                        }
                        R.id.signout -> {
                            AlertDialog.Builder(requireActivity())
                                .setTitle(R.string.are_you_suare)
                                .setPositiveButton(R.string.yes) { _, _ ->
                                    appAuth.removeAuth()
                                }
                                .setCancelable(true)
                                .setNegativeButton(R.string.no, null)
                                .create()
                                .show()
                            true
                        }
                        else -> false
                    }
                }
            }.apply {
                menuProvider = this
            }, viewLifecycleOwner)
        }
        binding.mainNavView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_posts -> {
                    findNavController().navigate(R.id.action_eventsFragment_to_feedFragment)
                    true
                }
                R.id.navigation_events -> {
                    true
                }
                R.id.navigation_users -> {
                    findNavController().navigate(R.id.action_eventsFragment_to_feedFragment)
                    findNavController().navigate(R.id.action_feedFragment_to_usersFragment)
                    true
                }
                R.id.navigation_profile -> {
                    findNavController().navigate(R.id.action_eventsFragment_to_feedFragment)
                    //findNavController().navigate(action_feedFragment_to_)  //TODO
                    true
                }
                else -> false
            }
        }
        binding.mainNavView.selectedItemId = R.id.navigation_events
        binding.fab.setOnClickListener {
            if (authViewModel.authenticated) {
                findNavController().navigate(R.id.action_eventsFragment_to_newEventFragment)
            } else {
                AlertDialog.Builder(context)
                    .setMessage(R.string.action_not_allowed)
                    .setPositiveButton(R.string.sign_up) { _, _ ->
                        findNavController().navigate(
                            R.id.action_eventsFragment_to_authFragment,
                            Bundle().apply {
                                textArg = getString(R.string.sign_up)
                            }
                        )
                    }
                    .setNeutralButton(R.string.sign_in) { _, _ ->
                        findNavController().navigate(
                            R.id.action_eventsFragment_to_authFragment,
                            Bundle().apply {
                                textArg = getString(R.string.sign_in)
                            }
                        )
                    }
                    .setNegativeButton(R.string.no, null)
                    .setCancelable(true)
                    .create()
                    .show()
            }
        }
        binding.swipe.setOnRefreshListener {
            viewModel.loadEvents()
            binding.swipe.isRefreshing = false
        }

        return binding.root
    }

    override fun onResume() {
        currentFragment = javaClass.simpleName
        super.onResume()
    }
}
