#import "AdAggregator.h"

@implementation AdAggregator
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeAdAggregatorSpecJSI>(params);
}

+ (NSString *)moduleName
{
  return @"AdAggregator";
}

@end
