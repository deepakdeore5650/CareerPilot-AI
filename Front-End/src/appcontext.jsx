import { createContext, useEffect, useState} from "react";

export const usercontext =createContext();

function Appcontext({children}){
    const apiBase = import.meta.env.VITE_API_BASE_URL || window.location.origin;
    const backendURL = `${apiBase}/resumeAnalyser/entry/v1`;
    const serviceURL = `${apiBase}/resumeAnalyserCore/service/v1`;
    const [islogged,setislogged]=useState(false)
    const [isprevious,setisprevious]=useState(false)
    const [username,setusername]=useState("")
    const [profilePhoto,setProfilePhoto]=useState("")
    const [isauthenticated,setisauthenticated]=useState(false)


   useEffect(() => {
        fetch(`${serviceURL}/isValid`, { method: "post", credentials: 'include' }).then(response => {
            if (response.ok) {
                return response.json()
                
            }
            else {
                setisauthenticated(true)
                return;
            }
        })
            .then(data => {
                if (data != null) {
                    setusername(data.username);
                    setisprevious(data.isPrevious)
                    setProfilePhoto(data.profilePhoto || "")
                    setislogged(true)
                    setisauthenticated(true)
                }
            }).catch(() => setisauthenticated(true))

    }, [])


    const arr ={islogged,setislogged,isprevious,setisprevious,username,setusername,profilePhoto,setProfilePhoto,backendURL,serviceURL,isauthenticated}

    return (
        <usercontext.Provider value={arr}>
            {children}
        </usercontext.Provider>
    )
}

export default Appcontext