void VideoRendererBase::ThreadMain() {
  base::PlatformThread::SetName("CrVideoRenderer");

  const base::TimeDelta kIdleTimeDelta =
      base::TimeDelta::FromMilliseconds(10);

  uint32 frames_dropped = 0;

  for (;;) {
    if (frames_dropped > 0) {
      PipelineStatistics statistics;
      statistics.video_frames_dropped = frames_dropped;
      statistics_cb_.Run(statistics);

      frames_dropped = 0;
    }

    base::AutoLock auto_lock(lock_);

    if (state_ == kStopped)
      return;

    if (state_ != kPlaying || playback_rate_ == 0) {
      frame_available_.TimedWait(kIdleTimeDelta);
      continue;
    }

    if (ready_frames_.empty()) {
      frame_available_.TimedWait(kIdleTimeDelta);
      continue;
    }

    if (!current_frame_) {
      if (ready_frames_.front()->IsEndOfStream()) {
        state_ = kEnded;
        host()->NotifyEnded();
        ready_frames_.clear();

        continue;
      }

      frame_available_.TimedWait(kIdleTimeDelta);
      continue;
    }

    base::TimeDelta remaining_time =
        CalculateSleepDuration(ready_frames_.front(), playback_rate_);

    if (remaining_time.InMicroseconds() > 0) {
      remaining_time = std::min(remaining_time, kIdleTimeDelta);
      frame_available_.TimedWait(remaining_time);
      continue;
    }




    if (ready_frames_.front()->IsEndOfStream()) {
      state_ = kEnded;
      host()->NotifyEnded();
      ready_frames_.clear();

      continue;
    }

    if (pending_paint_) {
      while (!ready_frames_.empty()) {
        if (ready_frames_.front()->IsEndOfStream())
          break;

        base::TimeDelta remaining_time =
            ready_frames_.front()->GetTimestamp() - host()->GetTime();

        if (remaining_time.InMicroseconds() > 0)
          break;

        if (!drop_frames_)
          break;

        ++frames_dropped;
        ready_frames_.pop_front();
        AttemptRead_Locked();
      }
      frame_available_.TimedWait(kIdleTimeDelta);
      continue;
    }


    DCHECK(!pending_paint_);
    DCHECK(!ready_frames_.empty());
    current_frame_ = ready_frames_.front();
    ready_frames_.pop_front();
    AttemptRead_Locked();

    base::AutoUnlock auto_unlock(lock_);
    paint_cb_.Run();
  }
}
