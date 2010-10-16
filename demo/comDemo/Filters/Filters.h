#pragma once

#define DECLARE_PTR(type, ptr, expr) type* ptr = (type*)(expr);
#include "CAutoDC.h"
#include "Red5Com.h"
EXTERN_C const GUID CLSID_FlexCOM;

class CVCamStream;
class CVCam : public CSource
{
public:
    //////////////////////////////////////////////////////////////////////////
    //  IUnknown
    //////////////////////////////////////////////////////////////////////////
    static CUnknown * WINAPI CreateInstance(LPUNKNOWN lpunk, HRESULT *phr);
    STDMETHODIMP QueryInterface(REFIID riid, void **ppv);

    IFilterGraph *GetGraph() {return m_pGraph;}

private:

    CVCam(LPUNKNOWN lpunk, HRESULT *phr);
};

class CVCamStream : public CSourceStream, public IAMDroppedFrames,public IAMStreamConfig, public IKsPropertySet
{
public:

    //////////////////////////////////////////////////////////////////////////
    //  IUnknown
    //////////////////////////////////////////////////////////////////////////
    STDMETHODIMP QueryInterface(REFIID riid, void **ppv);
    STDMETHODIMP_(ULONG) AddRef() { return GetOwner()->AddRef(); }                                                          \
    STDMETHODIMP_(ULONG) Release() { return GetOwner()->Release(); }

    //////////////////////////////////////////////////////////////////////////
    //  IQualityControl
    //////////////////////////////////////////////////////////////////////////
    STDMETHODIMP Notify(IBaseFilter * pSender, Quality q);

    //////////////////////////////////////////////////////////////////////////
    //  IAMStreamConfig
    //////////////////////////////////////////////////////////////////////////
    HRESULT STDMETHODCALLTYPE SetFormat(AM_MEDIA_TYPE *pmt);
    HRESULT STDMETHODCALLTYPE GetFormat(AM_MEDIA_TYPE **ppmt);
    HRESULT STDMETHODCALLTYPE GetNumberOfCapabilities(int *piCount, int *piSize);
    HRESULT STDMETHODCALLTYPE GetStreamCaps(int iIndex, AM_MEDIA_TYPE **pmt, BYTE *pSCC);
	//////////////////////////////////////////////////////////////////////////
    //  IAMDroppedFrames
    //////////////////////////////////////////////////////////////////////////
    HRESULT STDMETHODCALLTYPE GetAverageFrameSize( long* plAverageSize);
	HRESULT STDMETHODCALLTYPE GetDroppedInfo(long  lSize,long* plArray,long* plNumCopied);
	HRESULT STDMETHODCALLTYPE GetNumDropped(long *plDropped);
	HRESULT STDMETHODCALLTYPE GetNumNotDropped(long *plNotDropped);
    //////////////////////////////////////////////////////////////////////////
    //  IKsPropertySet
    //////////////////////////////////////////////////////////////////////////
    HRESULT STDMETHODCALLTYPE Set(REFGUID guidPropSet, DWORD dwID, void *pInstanceData, DWORD cbInstanceData, void *pPropData, DWORD cbPropData);
    HRESULT STDMETHODCALLTYPE Get(REFGUID guidPropSet, DWORD dwPropID, void *pInstanceData,DWORD cbInstanceData, void *pPropData, DWORD cbPropData, DWORD *pcbReturned);
    HRESULT STDMETHODCALLTYPE QuerySupported(REFGUID guidPropSet, DWORD dwPropID, DWORD *pTypeSupport);
    
    //////////////////////////////////////////////////////////////////////////
    //  CSourceStream
    //////////////////////////////////////////////////////////////////////////
    CVCamStream(HRESULT *phr, CVCam *pParent, LPCWSTR pPinName);
    ~CVCamStream();

    HRESULT FillBuffer(IMediaSample *pms);
    HRESULT DecideBufferSize(IMemAllocator *pIMemAlloc, ALLOCATOR_PROPERTIES *pProperties);
    HRESULT CheckMediaType(const CMediaType *pMediaType);
    HRESULT GetMediaType(int iPosition, CMediaType *pmt);
    HRESULT SetMediaType(const CMediaType *pmt);
    HRESULT OnThreadCreate(void);
    
private:
	long  NumDroppedFrames,NumFrames;
	
    CVCam *m_pParent;
    REFERENCE_TIME 
		m_rtLastTime,//running timestamp
		refSync1,// Graphmanager clock time, to compute dropped frames.
		refSync2,// Clock time for Sleeping each frame if not dropping.
		refStart,// Real time at start from Graphmanager clock time.
		rtStreamOff;// IAMPushSource Get/Set data member.
    HBITMAP m_hLogoBmp;
    CCritSec m_cSharedState;
    IReferenceClock *m_pClock;
	CAutoDC * adc;
	Red5Com * r5c;
};


