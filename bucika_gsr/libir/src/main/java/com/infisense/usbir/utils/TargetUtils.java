package com.infisense.usbir.utils;

import com.infisense.usbir.R;
import com.topdon.lib.core.bean.ObserveBean;

public class TargetUtils {
    public static int getSelectTargetDraw(int targetMeasureMode, int targetType, int targetColorType){
        int currentSelectDraw = R.drawable.svg_ic_target_horizontal_person_green;
        if(targetColorType == ObserveBean.TYPE_TARGET_COLOR_GREEN){
           if(targetMeasureMode == ObserveBean.TYPE_MEASURE_PERSON){
               if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
                   currentSelectDraw = R.drawable.svg_ic_target_horizontal_person_green;
               } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
                   currentSelectDraw = R.drawable.ic_target_vertical_person_green;
               } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
                   currentSelectDraw = R.drawable.ic_target_circle_person_green;
               }
           } else if(targetMeasureMode == ObserveBean.TYPE_MEASURE_SHEEP){
               if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
                   currentSelectDraw = R.drawable.ic_target_horizontal_sheep_green;
               } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
                   currentSelectDraw = R.drawable.ic_target_vertical_sheep_green;
               } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
                   currentSelectDraw = R.drawable.ic_target_circle_sheep_green;
               }
           } else if(targetMeasureMode == ObserveBean.TYPE_MEASURE_DOG){
//               if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
//                   currentSelectDraw = R.drawable.ic_target_horizontal_dog_green;
//               } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
//                   currentSelectDraw = R.drawable.ic_target_vertical_dog_green;
//               } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
//                   currentSelectDraw = R.drawable.ic_target_circle_dog_green;
//               }
               if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
                   currentSelectDraw = R.drawable.ic_target_horizontal_sheep_green;
               } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
                   currentSelectDraw = R.drawable.ic_target_vertical_sheep_green;
               } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
                   currentSelectDraw = R.drawable.ic_target_circle_sheep_green;
               }
           } else if(targetMeasureMode == ObserveBean.TYPE_MEASURE_BIRD){
//               if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
//                   currentSelectDraw = R.drawable.ic_target_horizontal_bird_green;
//               } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
//                   currentSelectDraw = R.drawable.ic_target_vertical_bird_green;
//               } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
//                   currentSelectDraw = R.drawable.ic_target_circle_bird_green;
//               }
               if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
                   currentSelectDraw = R.drawable.ic_target_horizontal_sheep_green;
               } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
                   currentSelectDraw = R.drawable.ic_target_vertical_sheep_green;
               } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
                   currentSelectDraw = R.drawable.ic_target_circle_sheep_green;
               }
           }
        } else if(targetColorType == ObserveBean.TYPE_TARGET_COLOR_RED){
            if(targetMeasureMode == ObserveBean.TYPE_MEASURE_PERSON){
                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
                    currentSelectDraw = R.drawable.ic_target_horizontal_person_red;
                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
                    currentSelectDraw = R.drawable.ic_target_vertical_person_red;
                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
                    currentSelectDraw = R.drawable.ic_target_circle_person_red;
                }
            } else if(targetMeasureMode == ObserveBean.TYPE_MEASURE_SHEEP){
                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
                    currentSelectDraw = R.drawable.ic_target_horizontal_sheep_red;
                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
                    currentSelectDraw = R.drawable.ic_target_vertical_sheep_red;
                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
                    currentSelectDraw = R.drawable.ic_target_circle_sheep_red;
                }
            } else if(targetMeasureMode == ObserveBean.TYPE_MEASURE_DOG){
//                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
//                    currentSelectDraw = R.drawable.ic_target_horizontal_dog_red;
//                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
//                    currentSelectDraw = R.drawable.ic_target_vertical_dog_red;
//                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
//                    currentSelectDraw = R.drawable.ic_target_circle_dog_red;
//                }
                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
                    currentSelectDraw = R.drawable.ic_target_horizontal_sheep_red;
                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
                    currentSelectDraw = R.drawable.ic_target_vertical_sheep_red;
                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
                    currentSelectDraw = R.drawable.ic_target_circle_sheep_red;
                }
            } else if(targetMeasureMode == ObserveBean.TYPE_MEASURE_BIRD){
//                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
//                    currentSelectDraw = R.drawable.ic_target_horizontal_bird_red;
//                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
//                    currentSelectDraw = R.drawable.ic_target_vertical_bird_red;
//                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
//                    currentSelectDraw = R.drawable.ic_target_circle_bird_red;
//                }
                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
                    currentSelectDraw = R.drawable.ic_target_horizontal_sheep_red;
                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
                    currentSelectDraw = R.drawable.ic_target_vertical_sheep_red;
                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
                    currentSelectDraw = R.drawable.ic_target_circle_sheep_red;
                }
            }
        } else if(targetColorType == ObserveBean.TYPE_TARGET_COLOR_BLUE){
            if(targetMeasureMode == ObserveBean.TYPE_MEASURE_PERSON){
                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
                    currentSelectDraw = R.drawable.ic_target_horizontal_person_blue;
                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
                    currentSelectDraw = R.drawable.ic_target_vertical_person_blue;
                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
                    currentSelectDraw = R.drawable.ic_target_circle_person_blue;
                }
            } else if(targetMeasureMode == ObserveBean.TYPE_MEASURE_SHEEP){
                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
                    currentSelectDraw = R.drawable.ic_target_horizontal_sheep_blue;
                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
                    currentSelectDraw = R.drawable.ic_target_vertical_sheep_blue;
                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
                    currentSelectDraw = R.drawable.ic_target_circle_sheep_blue;
                }
            } else if(targetMeasureMode == ObserveBean.TYPE_MEASURE_DOG){
//                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
//                    currentSelectDraw = R.drawable.ic_target_horizontal_dog_blue;
//                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
//                    currentSelectDraw = R.drawable.ic_target_vertical_dog_blue;
//                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
//                    currentSelectDraw = R.drawable.ic_target_circle_dog_blue;
//                }
                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
                    currentSelectDraw = R.drawable.ic_target_horizontal_sheep_blue;
                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
                    currentSelectDraw = R.drawable.ic_target_vertical_sheep_blue;
                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
                    currentSelectDraw = R.drawable.ic_target_circle_sheep_blue;
                }
            } else if(targetMeasureMode == ObserveBean.TYPE_MEASURE_BIRD){
//                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
//                    currentSelectDraw = R.drawable.ic_target_horizontal_bird_blue;
//                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
//                    currentSelectDraw = R.drawable.ic_target_vertical_bird_blue;
//                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
//                    currentSelectDraw = R.drawable.ic_target_circle_bird_blue;
//                }
                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
                    currentSelectDraw = R.drawable.ic_target_horizontal_sheep_blue;
                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
                    currentSelectDraw = R.drawable.ic_target_vertical_sheep_blue;
                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
                    currentSelectDraw = R.drawable.ic_target_circle_sheep_blue;
                }
            }
        } else if(targetColorType == ObserveBean.TYPE_TARGET_COLOR_BLACK){
            if(targetMeasureMode == ObserveBean.TYPE_MEASURE_PERSON){
                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
                    currentSelectDraw = R.drawable.ic_target_horizontal_person_black;
                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
                    currentSelectDraw = R.drawable.ic_target_vertical_person_black;
                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
                    currentSelectDraw = R.drawable.ic_target_circle_person_black;
                }
            } else if(targetMeasureMode == ObserveBean.TYPE_MEASURE_SHEEP){
                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
                    currentSelectDraw = R.drawable.ic_target_horizontal_sheep_black;
                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
                    currentSelectDraw = R.drawable.ic_target_vertical_sheep_black;
                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
                    currentSelectDraw = R.drawable.ic_target_circle_sheep_black;
                }
            } else if(targetMeasureMode == ObserveBean.TYPE_MEASURE_DOG){
//                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
//                    currentSelectDraw = R.drawable.ic_target_horizontal_dog_black;
//                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
//                    currentSelectDraw = R.drawable.ic_target_vertical_dog_black;
//                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
//                    currentSelectDraw = R.drawable.ic_target_circle_dog_black;
//                }
                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
                    currentSelectDraw = R.drawable.ic_target_horizontal_sheep_black;
                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
                    currentSelectDraw = R.drawable.ic_target_vertical_sheep_black;
                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
                    currentSelectDraw = R.drawable.ic_target_circle_sheep_black;
                }
            } else if(targetMeasureMode == ObserveBean.TYPE_MEASURE_BIRD){
//                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
//                    currentSelectDraw = R.drawable.ic_target_horizontal_bird_black;
//                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
//                    currentSelectDraw = R.drawable.ic_target_vertical_bird_black;
//                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
//                    currentSelectDraw = R.drawable.ic_target_circle_bird_black;
//                }
                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
                    currentSelectDraw = R.drawable.ic_target_horizontal_sheep_black;
                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
                    currentSelectDraw = R.drawable.ic_target_vertical_sheep_black;
                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
                    currentSelectDraw = R.drawable.ic_target_circle_sheep_black;
                }
            }
        } else if(targetColorType == ObserveBean.TYPE_TARGET_COLOR_WHITE){
            if(targetMeasureMode == ObserveBean.TYPE_MEASURE_PERSON){
                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
                    currentSelectDraw = R.drawable.ic_target_horizontal_person_white;
                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
                    currentSelectDraw = R.drawable.ic_target_vertical_person_white;
                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
                    currentSelectDraw = R.drawable.ic_target_circle_person_white;
                }
            } else if(targetMeasureMode == ObserveBean.TYPE_MEASURE_SHEEP){
                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
                    currentSelectDraw = R.drawable.ic_target_horizontal_sheep_white;
                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
                    currentSelectDraw = R.drawable.ic_target_vertical_sheep_white;
                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
                    currentSelectDraw = R.drawable.ic_target_circle_sheep_white;
                }
            } else if(targetMeasureMode == ObserveBean.TYPE_MEASURE_DOG){
//                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
//                    currentSelectDraw = R.drawable.ic_target_horizontal_dog_white;
//                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
//                    currentSelectDraw = R.drawable.ic_target_vertical_dog_white;
//                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
//                    currentSelectDraw = R.drawable.ic_target_circle_dog_white;
//                }
                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
                    currentSelectDraw = R.drawable.ic_target_horizontal_sheep_white;
                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
                    currentSelectDraw = R.drawable.ic_target_vertical_sheep_white;
                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
                    currentSelectDraw = R.drawable.ic_target_circle_sheep_white;
                }
            } else if(targetMeasureMode == ObserveBean.TYPE_MEASURE_BIRD){
//                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
//                    currentSelectDraw = R.drawable.ic_target_horizontal_bird_white;
//                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
//                    currentSelectDraw = R.drawable.ic_target_vertical_bird_white;
//                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
//                    currentSelectDraw = R.drawable.ic_target_circle_bird_white;
//                }
                if(targetType == ObserveBean.TYPE_TARGET_HORIZONTAL){
                    currentSelectDraw = R.drawable.ic_target_horizontal_sheep_white;
                } else if(targetType == ObserveBean.TYPE_TARGET_VERTICAL){
                    currentSelectDraw = R.drawable.ic_target_vertical_sheep_white;
                } else if(targetType == ObserveBean.TYPE_TARGET_CIRCLE){
                    currentSelectDraw = R.drawable.ic_target_circle_sheep_white;
                }
            }
        }
        return currentSelectDraw;
    }

    public static float getMeasureSize(int targetMeasureMode){
        float mMeasureSize = 180f;
        switch (targetMeasureMode) {
            case ObserveBean.TYPE_MEASURE_PERSON://人
                mMeasureSize = 180f;
                break;
            case ObserveBean.TYPE_MEASURE_SHEEP://羊
                mMeasureSize = 100f;
                break;
            case ObserveBean.TYPE_MEASURE_DOG://狗
                mMeasureSize = 50f;
                break;
            case ObserveBean.TYPE_MEASURE_BIRD://鸟
                mMeasureSize = 20f;
                break;
        }
        return mMeasureSize;
    }

    public static boolean isScaleMode(int targetMeasureMode){
        boolean isScaleFlag = false;
        if(targetMeasureMode == ObserveBean.TYPE_MEASURE_DOG ||
                targetMeasureMode == ObserveBean.TYPE_MEASURE_BIRD){
            isScaleFlag = true;
        }
        return isScaleFlag;
    }
}
